package com.tjapmerak.conclave;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.tjapmerak.conclave.adapters.NodeListAdapter;
import com.tjapmerak.conclave.fragments.LoginDialogFragment;
import com.tjapmerak.conclave.models.Node;
import com.tjapmerak.conclave.services.CommandService;
import com.tjapmerak.conclave.services.SetupService;
import com.tjapmerak.conclave.utils.SSHManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements LoginDialogFragment.NoticeDialogListener {

    private static final String TAG = MainActivity.class.getName();

    private TextView mTextView;
    private ListView mNodeListView;

    private NodeListAdapter mNodeListAdapter;
    private CommandService mCommandService;
    private Messenger mMessenger;
    private boolean mBound;

    private DB mSnappyDb;
    private SharedPreferences mSharedPref;
    private SSHManager mSsh;

    private String username;
    private String password;
    private String host;
    // TODO: what is known host? (consider)
    private String knownHost = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openSnappy();

        mTextView = (TextView) findViewById(R.id.cocote);
        mNodeListView = (ListView) findViewById(R.id.node_listview);

        // get shared preferences value to indicate if the app had configured
        mSharedPref = getPreferences(Context.MODE_PRIVATE);
        if (!mSharedPref.getBoolean("is_configured", false)) {
            // show dialog to ask main node configuration
            LoginDialogFragment dialog = new LoginDialogFragment();
            dialog.show(getFragmentManager(), "login");
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("is_configured", true);
            editor.apply();
        }

        populateNodeList();
        doContextualCommand();

    }

    private void populateNodeList() {
        // TODO: get node list and it's command from snappydb
        String nodes[];
        List<Node> nodeList = new ArrayList<>();
        try {
            nodes = mSnappyDb.getObjectArray("nodelist", String.class);
            for (String node : nodes) {
                String groups[] = node.split("__");
                String key = groups[1] + ":" + groups[0];
                int power = mSnappyDb.getInt(key + ":power");
                String command = mSnappyDb.get(key + ":command");
                Node n = new Node(groups[1], groups[0], command, 0);
                nodeList.add(n);
            }
            createNodeAdapter(nodeList);
        } catch (SnappydbException sX) {
            Log.e(TAG, sX.getMessage());
        }
    }

    private void createNodeAdapter(List<Node> nodeList) {
        mNodeListAdapter = new NodeListAdapter(this, nodeList, mCommandService);
        mNodeListView.setAdapter(mNodeListAdapter);
    }

    private void doContextualCommand() {
        username = mSharedPref.getString("username", "");
        password = mSharedPref.getString("password", "");
        host = mSharedPref.getString("host", "");

        // start CommandService
//        Intent service = new Intent(this, CommandService.class);
//        service.putExtra("username", username);
//        service.putExtra("password", password);
//        service.putExtra("host", host);
//        service.putExtra("knownHost", knownHost);

//        service.putExtra("command", "conclave.py --node=lamp --id=1 && ...");
//        mCommandService.executeCommand()

    }

    private void doSetup() {
        // Setup Code
        // retrieve node list from R-PI
        // start SetupService
        Intent service = new Intent(this, SetupService.class);
        service.putExtra("username", username);
        service.putExtra("password", password);
        service.putExtra("host", host);
        service.putExtra("knownHost", knownHost);

        startService(service);
        // look onReceive for persist logic
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, CommandService.class);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("host", host);
        intent.putExtra("knownHost", knownHost);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(setupReceiver, new IntentFilter(SetupService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(setupReceiver);
        closeSnappy();
    }

    private void openSnappy() {
        try {
            mSnappyDb = DBFactory.open(this);
        } catch (SnappydbException sX) {
            Log.e(TAG, sX.getMessage());
        }
    }

    private void closeSnappy(){
        try {
            mSnappyDb.close();
        } catch (SnappydbException sX) {
            Log.e(TAG, sX.getMessage());
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        LoginDialogFragment dlg = (LoginDialogFragment) dialog;
        username = dlg.getUsername();
        password = dlg.getPassword();
        host = dlg.getHost();

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("host", host);
        editor.apply();

        doSetup();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommandService.MyBinder binder = (CommandService.MyBinder) service;
            mCommandService =  binder.getService();
            mMessenger = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            mBound = false;
        }
    };

    private BroadcastReceiver setupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String output = bundle.getString("output");

                mTextView.setText(output);

                // TODO: serialize output (persist)
                String lines[] = output.split("\\r?\\n");
                for (String line : lines) {
                    String groups[] = line.split("__");
                    String nodeName = groups[0];
                    String nodeId = groups[1];

                    // interpolate basic command with off state
                    String command = interpolateCommand(nodeName, nodeId, 0);

                    try {
                        String key = nodeName + ":" + nodeId;
                        mSnappyDb.putInt(key + ":power", 0);
                        mSnappyDb.put(key+":command", command);
                    } catch (SnappydbException sX) {
                        Log.e(TAG, sX.getMessage());
                    }
                }

                try {
                    mSnappyDb.put("nodelist", lines);
                } catch (SnappydbException sX) {
                    Log.e(TAG, sX.getMessage());
                }
            }
        }
    };

    private String interpolateCommand(String nodeName, String nodeId, int power) {
        String ret =  "python conclave.py"
                + " " + "--node=" + nodeName
                + " " + "--id=" + nodeId
                + " " + "--power=" + power;
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.tjapmerak.conclave.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tjapmerak.conclave.utils.SSHManager;

/**
 * Created by bahrunnur on 3/7/15.
 */
public class SetupService extends IntentService {

    private static final String TAG = SetupService.class.getName();

    public static final String COMMAND = "python conclave.py";
    public static final String NOTIFICATION = "com.tjapmerak.conclave";

    public SetupService() {
        super("SetupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // exexute main node ssh process
        Log.d(TAG, "wkwk");
        Bundle extras = intent.getExtras();
        String username = extras.getString("username");
        String password = extras.getString("password");
        String host = extras.getString("host");
        String knownHost = extras.getString("knownHost");

        SSHManager ssh = new SSHManager(username, password, host, knownHost);
        String err = ssh.connect();

        if (err != null) {
            Log.w(TAG, err);
        }

        String output = ssh.sendCommand(COMMAND);
        publishResults(output);
    }

    private void publishResults(String output) {
        Intent i = new Intent(NOTIFICATION);
        i.putExtra("output", output);
        sendBroadcast(i);
    }
}

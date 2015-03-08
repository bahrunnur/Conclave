package com.tjapmerak.conclave.adapters;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.tjapmerak.conclave.R;
import com.tjapmerak.conclave.models.Node;
import com.tjapmerak.conclave.services.CommandService;

import java.util.List;

/**
 * Created by bahrunnur on 3/7/15.
 */
public class NodeListAdapter extends ArrayAdapter<Node> {

    private final Context mContext;
    private final List<Node> mNodeList;
    private final CommandService mService;

    private LayoutInflater mInflater;

    public NodeListAdapter(Context context, List<Node> nodeList, CommandService service) {
        super(context, R.layout.node_item, nodeList);
        mContext = context;
        mNodeList = nodeList;
        mService = service;
    }

    @Override
    public int getCount() {
        return mNodeList.size();
    }

    @Override
    public Node getItem(int position) {
        return mNodeList.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        // this is bullshit
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = (LinearLayout) mInflater.inflate(R.layout.node_item, null);

            NodeViewHolder holder = new NodeViewHolder();
            holder.nodeSwitch = (Switch) convertView.findViewById(R.id.node_item_switch);

            convertView.setTag(holder);
        }

        final Node currentItem = getItem(position);
        NodeViewHolder holder = (NodeViewHolder) convertView.getTag();

        final boolean state = currentItem.getPower() == 1;
        holder.nodeSwitch.setChecked(state);
        holder.nodeSwitch.setText(currentItem.getName() + " " + currentItem.getId());

        // TODO: set switch check listener
        holder.nodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String command;
                if (state && !isChecked) {
                    currentItem.setPower(0);
                } else if (!state && isChecked) {
                    currentItem.setPower(1);
                }
                command = currentItem.interpolateCommand();
                mService.executeCommand(command);
            }
        });

        return convertView;
    }

    static class NodeViewHolder {
        Switch nodeSwitch;
    }

}

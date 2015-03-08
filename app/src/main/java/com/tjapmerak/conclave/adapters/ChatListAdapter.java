package com.tjapmerak.conclave.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by bahrunnur on 3/6/15.
 */
public class ChatListAdapter extends ArrayAdapter {

    public ChatListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}

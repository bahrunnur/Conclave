package com.tjapmerak.conclave.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.tjapmerak.conclave.utils.SSHManager;

/**
 * Created by bahrunnur on 3/6/15.
 */
public class CommandService extends Service {

    private final IBinder mBinder = new MyBinder();

    private SSHManager ssh;

    private String output;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // exexute main node ssh process
        Bundle extras = intent.getExtras();
        String username = extras.getString("username");
        String password = extras.getString("password");
        String host = extras.getString("host");
        String knownHost = extras.getString("knownHost");
        ssh = new SSHManager(username, password, host, knownHost);
        ssh.connect();

//        String command = extras.getString("command");
//        output = ssh.sendCommand(command);

        return Service.START_NOT_STICKY;
    }

    public String executeCommand(String command) {
        return ssh.sendCommand(command);
    }

    public String getOutput() {
        return output;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public CommandService getService() {
            return CommandService.this;
        }
    }
}

package com.tjapmerak.conclave.utils;

/**
 * Created by bahrunnur on 3/4/15.
 */
/*
 * SSHManager (taken from http://stackoverflow.com/a/11902536)
 *
 * @author cabbott
 * @version 1.0
 */

import android.util.Log;

import com.jcraft.jsch.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHManager
{
    private static final Logger LOGGER =
            Logger.getLogger(SSHManager.class.getName());
    private static final String TAG = SSHManager.class.getName();

    private JSch jschSSHChannel;
    private String strUserName;
    private String strConnectionIP;
    private int intConnectionPort;
    private String strPassword;
    private Session sesConnection;
    private int intTimeOut;

    private void constructor(String userName, String password,
                             String connectionIP, String knownHostsFileName) {

        jschSSHChannel = new JSch();

//        try {
//            jschSSHChannel.setKnownHosts(knownHostsFileName);
//        } catch(JSchException jschX) {
//            logError(jschX.getMessage());
//        }

        strUserName = userName;
        strPassword = password;
        strConnectionIP = connectionIP;
    }

    public SSHManager(String userName, String password,
                      String connectionIP, String knownHostsFileName) {

        constructor(userName, password, connectionIP, knownHostsFileName);
        intConnectionPort = 22;
        intTimeOut = 120000;
    }

    public SSHManager(String userName, String password, String connectionIP,
                      String knownHostsFileName, int connectionPort) {

        constructor(userName, password, connectionIP, knownHostsFileName);
        intConnectionPort = connectionPort;
        intTimeOut = 120000;
    }

    public SSHManager(String userName, String password, String connectionIP,
                      String knownHostsFileName, int connectionPort, int timeOutMilliseconds) {

        constructor(userName, password, connectionIP, knownHostsFileName);
        intConnectionPort = connectionPort;
        intTimeOut = timeOutMilliseconds;
    }

    public String connect() {
        String errorMessage = null;

        try {
            sesConnection = jschSSHChannel.getSession(strUserName, strConnectionIP, intConnectionPort);
            sesConnection.setPassword(strPassword);
            // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION)
            sesConnection.setConfig("StrictHostKeyChecking", "no");
            sesConnection.connect(intTimeOut);
        } catch(JSchException jschX) {
            errorMessage = jschX.getMessage();
        }

        return errorMessage;
    }

    private String logError(String errorMessage) {
        if(errorMessage != null) {
            LOGGER.log(Level.SEVERE, "{0}:{1} - {2}",
                    new Object[]{strConnectionIP, intConnectionPort, errorMessage});
            Log.e(TAG, errorMessage);
        }

        return errorMessage;
    }

    private String logWarning(String warnMessage) {
        if(warnMessage != null) {
            LOGGER.log(Level.WARNING, "{0}:{1} - {2}",
                    new Object[]{strConnectionIP, intConnectionPort, warnMessage});
            Log.w(TAG, warnMessage);
        }

        return warnMessage;
    }

    public String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();

        try {
            Channel channel = sesConnection.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            InputStream commandOutput = channel.getInputStream();
            channel.connect();

            int readByte = commandOutput.read();

            while(readByte != 0xffffffff) {
                outputBuffer.append((char)readByte);
                readByte = commandOutput.read();
            }

            channel.disconnect();
        } catch(IOException ioX) {
            logWarning(ioX.getMessage());
            return null;
        } catch(JSchException jschX) {
            logWarning(jschX.getMessage());
            return null;
        }

        return outputBuffer.toString();
    }

    public void close() {
        sesConnection.disconnect();
    }

}

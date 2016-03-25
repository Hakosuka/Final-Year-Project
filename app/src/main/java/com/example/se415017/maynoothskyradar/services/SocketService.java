package com.example.se415017.maynoothskyradar.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.example.se415017.maynoothskyradar.activities.MainActivity;

import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import io.realm.annotations.PrimaryKey;

/**
 * This is a complete overhaul of the original SocketService class, in an attempt to get rid of any
 * redundant code.
 *
 * @version 10 December
 * @author Ciaran Cumiskey (se415017)
 */
public class SocketService extends Service {

    private Socket socket;

    /** Even if I move away from using a hardcoded address for the server, I'll still need to
     *  listen on port 30003.
     */
    static final int PORT = 30003; //redundant
    public static final int MESSAGE = 1;
    public static final int MSG_REG_CLIENT = 3;
    public static final int MSG_UNREG_CLIENT = 5;

    private static boolean isRunning = false;

    static final String SERVER = "sbsrv1.cs.nuim.ie"; //redundant
    static final String TAG = "SocketService";
    static final String PREFS = "UserPreferences";
    String serverAddr = ""; // should replace SERVER, is defined by the intent passed by MainActivity

    Intent bindingIntent;
    IBinder myBinder = new SimpleLocalBinder();
    Messenger messenger = new Messenger(new IncomingHandler());

    ArrayList<Messenger> messengerClientList = new ArrayList<>(); //keeps track of client classes
    Messenger replyMessenger;
    Message message;

    private boolean initialisationSuccess = false;
    private boolean urlReachable = false;
    private static boolean socketConnected = false;

    int response = 0;
    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        bindingIntent = intent;
        serverAddr = intent.getStringExtra("serverAddr");
        Log.d(TAG, "Server address = " + serverAddr);
        Log.d(TAG, "Returning binder");
        return messenger.getBinder();
    }

    public void initialiseSocket() {
        try {
            socket = new Socket(serverAddr, 30003);
            socket.connect(new InetSocketAddress(serverAddr, 30003), 10000); // Wait 10 secs before timing out
            Log.d(TAG, "Socket created = " + socket.toString());
            initialisationSuccess = !socket.isClosed();
            Log.d(TAG, "Initialisation successful");
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
    public void reconnectSocket(Socket socket) {
        if(socket!=null && initialisationSuccess){
            try {
                socket.connect(new InetSocketAddress(serverAddr, 30003), 10000);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * Starts running once constructed,
     */
    @Override
    public void onCreate(){
        Log.d(TAG, "Service created");
        super.onCreate();
        isRunning = true;
        myBinder = new Binder();
    }

    /**
     * Starts running as soon as startService(service) is called in a client
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    //This isn't being invoked...because I forgot I was using bindService, rather than startService (D'OH!)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        serverAddr = intent.getStringExtra("serverAddr");
        Log.d(TAG, "Address obtained = " + serverAddr);
        Log.d(TAG, "Service started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Network thread running");
                initialiseSocket();
                if(initialisationSuccess) { // && urlReachable) {
                    try {
                        InputStream inputStream = socket.getInputStream();
                        socketConnected = true;
                        readFromInputStream(inputStream);
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }).start();
        Log.d(TAG, "Returning service");
        return START_NOT_STICKY; // Don't bother restarting the Service if the device has ran out of memory
    }

    public static boolean isRunning() {
        Log.d("SocketService", "Is this running? " + Boolean.toString(isRunning));
        return isRunning;
    }

    public class SimpleLocalBinder extends Binder{
        public SocketService getService() {
            Log.d(TAG, "Binder is returning service");
            return SocketService.this;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(initialisationSuccess){
            try {
                socket.close();
                Log.d(TAG, "Socket closed? " + Boolean.toString(socket.isClosed())); // Just checking to see if the socket has actually closed
            }
            catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        isRunning = false;
    }

    @Override
    public boolean onUnbind(Intent unbindIntent){
        Log.d(TAG, "Socket service unbound");
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        super.onUnbind(unbindIntent);
        return true;
    }

    @Override
    public void onRebind(Intent rebindIntent){
        Log.d(TAG, "Socket service rebound");
        super.onRebind(rebindIntent);
        if(socket!=null){
                reconnectSocket(socket);
        }
    }
    /**
    * Takes in an InputStream, reads it and then returns the line.
    * @param inputStream - the stream derived from the socket
    * @return String builder.toString() - the result from the stream
    */
    public String readFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        Log.d(TAG, "About to try reading from BufferedReader");
        try {
            while(socketConnected){
                line = reader.readLine();
                builder.append(line);
                Log.d(TAG, "String from BufferedReader = " + line);
                sendMessageToClients(line);
//                if(replyMessenger != null){
//                    try {
//                        message.obj = line;
//                        replyMessenger.send(message);
//                    } catch (RemoteException e) {
//                        Log.e(TAG, e.toString());
//                    }
//                }
            }
        } catch (IOException e) {
            Log.e(TAG, "readFromInputStream error: " + e.toString());
        } finally { // runs clean-up code
            try {
                Log.d(TAG, "Closing InputStream");
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        return builder.toString();
    }

    /**
     * This sends the SBS-1 messages relayed to the app by the server to any client classes.
     * @param message The SBS-1 message picked up by the server.
     */
    private void sendMessageToClients(String message){
        Log.d(TAG, "Message to send = " + message);
        for(Messenger messenger : messengerClientList){
            try {
                Bundle bundle = new Bundle();
                bundle.putString("sbsMessage", message);
                Message msg = Message.obtain(null, MESSAGE);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                messengerClientList.remove(messenger);
            }
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MESSAGE:
                    Log.d(TAG, msg.toString());
                    break;
                case MSG_REG_CLIENT:
                    messengerClientList.add(msg.replyTo);
                    break;
                case MSG_UNREG_CLIENT:
                    messengerClientList.remove(msg.replyTo);
            }
        }
    }
}

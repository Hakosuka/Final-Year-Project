package com.example.se415017.maynoothskyradar.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketService extends Service {
    //TODO: get this working!
    public static final String DR_BROWNS_SERVER = "http://sbsrv1.cs.nuim.ie";
    public static final int SERVER_PORT = 30003;
    Socket socket;
    IBinder myBinder;
    InputStream inputStream;
    DataInputStream diStream;
    boolean socketConnected = false;
    boolean readerOpen = false;
    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        myBinder = new LocalBinder();
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            socket = new Socket(DR_BROWNS_SERVER, SERVER_PORT);
            socketConnected = true;
            inputStream = socket.getInputStream();
            diStream = new DataInputStream(inputStream);
            readerOpen = true;
            ConnectSocket connectSocket = new ConnectSocket();
            Log.d(getClass().toString(), "Socket created!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            diStream.close();
            readerOpen = false;
            socket.close();
            socketConnected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }

    class ConnectSocket implements Runnable {
        @Override
        public void run() {

            while(socketConnected && readerOpen){
                try {
                    int len = diStream.readInt();
                    byte[] streamResult = new byte[len];
                    if (len>0) {
                        diStream.readFully(streamResult);
                        Log.d(getClass().toString(), streamResult.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package com.example.se415017.maynoothskyradar.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketService extends Service {
    //TODO: get this working!
    private static final String TAG = "SocketService";
    public static final String DR_BROWNS_SERVER = "sbsrv1.cs.nuim.ie";
    public static final int SERVER_PORT = 30003;

    Socket socket;
    IBinder myBinder = new LocalBinder();
    InputStream inputStream;
    DataInputStream diStream;
    StringWriter writer;
    boolean socketConnected = false;
    boolean readerOpen = false;
    byte[] buffer = new byte[128];
    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
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
        if(writer == null) {
            writer = new StringWriter();
            Log.d(TAG, "New string writer created");
        }
        //TODO: NetworkOnMainThread error
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    socket = new Socket(DR_BROWNS_SERVER, SERVER_PORT);
                    socketConnected = true;
                    inputStream = socket.getInputStream();
                    IOUtils.copy(inputStream, writer, "UTF-8"); //TODO: socket exception
                    diStream = new DataInputStream(inputStream);
                    readerOpen = true;
                    ConnectSocket connectSocket = new ConnectSocket();
                    Log.d(getClass().toString(), "Socket created!");
                    Log.d(TAG, "readFromInputStream()" + readFromInputStream(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Socket failed");
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(socket != null && inputStream != null){
            try {
                inputStream.close();
                writer.close();
                readerOpen = false;
                socket.close();
                socketConnected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
    }

    public static String readFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader=  new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    class ConnectSocket implements Runnable {
        //TODO: This is probably causing a memory leak, I need to fix this
        @Override
        public void run() {
            Log.d(TAG, "ConnectSocket running");
            while(socketConnected && readerOpen){
                Log.d(TAG, "About to try reading from socket stream");
                try {
                    int len = diStream.readInt();
                    String streamString = IOUtils.toString(inputStream, "UTF-8");
                    /*//byte[] streamResult = new byte[len];
                    if (len>0) {

                        //Log.d(TAG, streamResult.toString());
                    }*/
                    Log.d(TAG, "DataInputStream: " + Integer.toString(len));
                    Log.d(TAG, "StringWriter from stream: " + streamString);
                    Log.d(TAG, "readFromInputStream()" + readFromInputStream(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Socket request failed", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

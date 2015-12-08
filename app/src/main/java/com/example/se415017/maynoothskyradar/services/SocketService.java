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
    BufferedReader bufferedReader;
    StringWriter writer;
    boolean firstReading = true; // checks if this is the first time the app is reading from the stream
    boolean socketConnected = false;
    boolean readerOpen = false;
    byte current;
    byte[] buffer = new byte[128];
    public SocketService() {
    }
    Intent bindingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        bindingIntent = intent;
        Log.d(TAG, "Returning binder");
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public SocketService getService() {
            if(writer == null) {
                writer = new StringWriter();
                Log.d(TAG, "New string writer created");
            }
            Log.d(TAG, "Returning SocketService");
            new Thread(new Runnable(){
                @Override
                public void run() {
                    Log.d(TAG, "New thread running");
                    try {
                        //TODO: clean this up
                        Log.d(TAG, "Beginning try block");
                        socket = new Socket(DR_BROWNS_SERVER, SERVER_PORT);
                        Log.d(TAG, "Socket created!");
                        socketConnected = true;
                        inputStream = socket.getInputStream();
                        Log.d(TAG, "Input stream initialised");
                        //IOUtils.copy(inputStream, writer, "UTF-8");
                        diStream = new DataInputStream(inputStream);
                        Log.d(TAG, "Data input stream initialised");
                        readerOpen = true;
                        current = (byte) inputStream.read();
                        Log.d(TAG + "byte", Byte.toString(current));
                        ConnectSocket connectSocket = new ConnectSocket();
                        Log.d(TAG, "connectSocket initialised");
                        while(readerOpen){
                            Log.d(TAG, "readFromInputStream()" + readFromInputStream(inputStream));
                            Log.d(TAG, diStream.readUTF());
                            connectSocket.run();
                        }
                    } catch (IOException e) {
                        //TODO: SocketException
                        Log.e(TAG, e.toString() + ". Socket failed.");
                    }
                }
            }).start();
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();



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
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n"); //TODO: fix System.err warning
                Log.d(TAG, "String from BufferedReader = " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    class ConnectSocket implements Runnable {
        //TODO: This is probably causing a memory leak, I need to fix this
        @Override
        public void run() {
            //Log.d(TAG, "ConnectSocket running");
            //while(socketConnected && readerOpen){
                Log.d(TAG, "About to try reading from socket stream");
                try {
                    Log.d(TAG, socket.toString());
                    socket = new Socket(DR_BROWNS_SERVER, SERVER_PORT);
                    socketConnected = true;
                    inputStream = socket.getInputStream();
                    IOUtils.copy(inputStream, writer, "UTF-8");
                    diStream = new DataInputStream(inputStream);
                    String diStreamResult = diStream.readUTF();
                    String streamString = IOUtils.toString(inputStream, "UTF-8");
                    /*//byte[] streamResult = new byte[len];
                    if (len>0) {

                        //Log.d(TAG, streamResult.toString());
                    }*/

                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    Log.d(TAG, "DataInputStream: " + diStreamResult);
                    Log.d(TAG, "StringWriter from stream: " + streamString);
                    Log.d(TAG, "readFromInputStream()" + readFromInputStream(inputStream));
                    Log.d(TAG, "bufferedReader from stream: " + bufferedReader.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Socket request failed", Toast.LENGTH_LONG).show();
                }
            //}
        }
    }
}

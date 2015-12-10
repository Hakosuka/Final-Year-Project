package com.example.se415017.maynoothskyradar.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * This is a complete overhaul of the original SocketService class, in an attempt to get rid of any
 * redundant code.
 *
 * @version 10 December
 * @author Ciaran Cumiskey (se415017)
 */
public class SocketService extends Service {

    private Socket socket;

    private static final int PORT = 30003;
    private static final String SERVER = "sbsrv1.cs.nuim.ie";
    private static final String TAG = "SocketService";
    Intent bindingIntent;
    IBinder myBinder = new SimpleLocalBinder();
    boolean initialisationSuccess = false;
    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        bindingIntent = intent;
        Log.d(TAG, "Returning binder");
        return myBinder;
    }
    //TODO: try and move this into getService()
    public void initialiseSocket() {
        try {
            socket = new Socket(SERVER, PORT);
            initialisationSuccess = true;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public class SimpleLocalBinder extends Binder{
        public SocketService getService() {
            Log.d(TAG, "Opening new thread for network operations");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initialiseSocket();
                    if(initialisationSuccess) {
                        try {
                            InputStream inputStream = socket.getInputStream();
                            readFromInputStream(inputStream);
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            }).start();
            Log.d(TAG, "Returning service");
            return SocketService.this;
        }
    }

    /**
    * Takes in an InputStream, reads it and then returns the line.
    * @params InputStream inputStream - the stream derived from the socket
    * @returns String builder.toString() - the result from the stream
    */
    public static String readFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        Log.d(TAG, "About to try reading from BufferedReader");
        try {
            //check that the line read in by the reader is neither null nor a 0-character string
            //while ((line = reader.readLine()) != null) {
            while(true){
                line = reader.readLine();
                builder.append(line); // + "\n"); removed because I think it might be causing unnecessary blank lines in the logcat
                Log.d(TAG, "String from BufferedReader = " + line);
            }
        } catch (IOException e) {
            Log.e(TAG, "readFromInputStream error: " + e.toString());
        } finally {
            try {
                Log.d(TAG, "Closing InputStream");
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        return builder.toString();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(initialisationSuccess){
            try {
                socket.close();
                Log.d(TAG, "Socket closed");
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}

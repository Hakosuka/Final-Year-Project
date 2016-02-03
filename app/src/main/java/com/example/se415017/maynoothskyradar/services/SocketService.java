package com.example.se415017.maynoothskyradar.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

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
    private static final int PORT = 30003;
    private static final String SERVER = "sbsrv1.cs.nuim.ie"; //TODO: make this hard-coded value redundant
    private static final String TAG = "SocketService";
    Intent bindingIntent;
    IBinder myBinder = new SimpleLocalBinder();
    boolean initialisationSuccess = false;
    boolean urlReachable = false;
    int response = 0;
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
            isURLReachable(SERVER);
            Log.d(TAG, "Initialisation successful");
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
                    Log.d(TAG, "Network thread running");
                    initialiseSocket();
                    if(initialisationSuccess && urlReachable) {
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
            while(true){
                line = reader.readLine();
                builder.append(line);
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

    /**
     * Checks if the server is up.
     * @params context, server - the address of the user's server, no need for http prefix
     * @return serverStatus - whether the server is up or not
     */
    public boolean isURLReachable(String serverAddr) {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        //if(networkInfo != null && networkInfo.isConnected()) {
        // The above code is redundant, because we wouldn't have reached this point had we not
        // detected any network connection while initialising this SocketService in MainActivity
            try {
                URL serverURL = new URL("http", serverAddr, PORT, "");
                HttpURLConnection urlc = (HttpURLConnection) serverURL.openConnection();
                urlc.setConnectTimeout(10000); // 10,000ms or 10s
                urlc.connect();
                response = urlc.getResponseCode();
                Log.d(TAG, "Response from " + serverAddr + ": " + Integer.toString(response));
                if (response == 200) {
                    Log.d(TAG, "Connection is of great success, high five!");
                    Toast.makeText(getApplicationContext(), "Connection successful", Toast.LENGTH_LONG).show();
                    urlReachable = true;
                    return true;
                } else {
                    Log.d(TAG, "Server unavailable.");
                    Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();
                    return false;
                }
            } catch (MalformedURLException e1) {
                Log.e(TAG, e1.toString());
                return false;
            } catch (IOException e2) {
                Log.e(TAG, e2.toString());
                return false;
            }
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

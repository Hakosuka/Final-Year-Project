package com.example.se415017.maynoothskyradar.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

/**
 * Created by se415017 on 09/11/2015.
 *
 * This checks if the user has an Internet connection.
 */
public class NetHelper {

    ConnectivityManager connMgr;
    NetworkInfo netInfo;
    int response = 0;
    String TAG = "NetHelper";

    public NetHelper(Context context){
        connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = connMgr.getActiveNetworkInfo();
    }
    public boolean isConnected() {
        netInfo = connMgr.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.d(TAG, netInfo.toString()); //TODO: Test if this creates a NullPointerException
            return true;
        }
        else {
            Log.d(TAG, "No available network info");
            return false;
        }
    }

    // Checks if the user can connect to the server
    // 1 February 2016 - added a parameter to allow for user-entered URLs
    // 2 February 2016 - goddamn NetworkOnMainThread exceptions
    // 3 February 2016 - I forgot about start() for threads. Goddammit.
    // 4 February 2016 - Replaced with checking for the server in SocketService. Now redundant.
    /*public boolean serverIsUp(final URL url) {
        Log.d(TAG, "About to start new thread");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New thread running for serverIsUp()");
                HttpURLConnection urlConnection = null;
                try {
                    //URL url = new URL("http://sbsrv1.cs.nuim.ie:30003"); moving away from hard-coded URL
                    Log.d(TAG, "Trying to open connection");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(5000);
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    response = urlConnection.getResponseCode();
                    Log.d(TAG, "Response = " + Integer.toString(response));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(urlConnection != null){
                        urlConnection.disconnect();
                        Log.d(TAG, "Check finished");
                    }
                }
            }
        }).start();
        Log.d(TAG, "Response = " + Integer.toString(response));
        return(response >= 200 && response <= 399);
    }*/
}



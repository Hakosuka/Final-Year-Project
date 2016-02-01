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

    public NetHelper(Context context){
        connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = connMgr.getActiveNetworkInfo();
    }
    public boolean isConnected() {
        netInfo = connMgr.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.d("Connected to", netInfo.toString()); //NullPointerException
            return true;
        }
        else {
            Log.d("netInfo = ", "No available network info");
            return false;
        }
    }

    // Checks if the user can connect to the server
    // 1 February 2016 - added a parameter to allow for user-entered URLs
    public Boolean serverIsUp(URL url) {
        HttpURLConnection urlConnection = null;
        try {
            //URL url = new URL("http://sbsrv1.cs.nuim.ie:30003"); moving away from hard-coded URL
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int response = urlConnection.getResponseCode();
            Log.d(getClass().toString(), "Response = " + Integer.toString(response));
            return (response >= 200 && response <= 399);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
    }
}



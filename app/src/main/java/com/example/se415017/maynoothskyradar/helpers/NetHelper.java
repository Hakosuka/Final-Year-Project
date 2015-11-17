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
        Log.d("Connected to", netInfo.toString());
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean checkIfServerIsUp() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("http://sbsrv1.cs.nuim.ie:30003").openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode <= 399);
        } catch (IOException e) {
            Log.e("Checking for server", e.toString());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

}

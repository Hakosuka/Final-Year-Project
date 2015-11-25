package com.example.se415017.maynoothskyradar.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.helpers.NetHelper;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Ciaran Cumiskey se415017 #12342236
 * @version 11 November 2015
 * Parsing the GPS data supplied by Joe in his email.
 */
public class MainActivity extends AppCompatActivity {
    //TODO: move all of the UI stuff out of the Activity and into Fragments
    private String strUrl = "http://sbsrv1.cs.nuim.ie";
    private int serverPort = 30003;

    @Bind(R.id.button_gps_activation)
    Button GpsActivationButton;
    @Bind(R.id.check_server_button)
    Button CheckServerButton;

    //These are the GPS coordinates of the server
    @Bind(R.id.gps_latitude)
    TextView GpsLat;
    @Bind(R.id.gps_longitude)
    TextView GpsLon;

    @Bind(R.id.server_status)
    TextView ServerStat;

    @OnClick(R.id.button_gps_activation)
    public void activateGPS(View view){
        Log.d("activateGPS", "Button pressed");
        for(int i = 0; i < dummyData.length; i++){
            decodeNMEA(dummyData[i]);
        }
    }

    @OnClick(R.id.check_server_button)
    public void checkServerHandler(View view){
        Log.d("checkServer", "Button pressed");
        NetHelper netHelper = new NetHelper(getApplicationContext());
        if(netHelper.isConnected()) {
            Log.d("checkServer", "Connection available");
            ServerStat.setText("Server is available");
        }
        else {
            Log.d("checkServer", "No connection available");
            new MaterialDialog.Builder(this).title("No Internet connection available")
                    .content("Please activate your mobile data or connect to wi-fi.")
                    .cancelable(false)
                    .positiveText("Open settings")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    });

        }
    }

    boolean netStatus = false;
    Boolean serverStatus = false;
    String[] dummyData = {
            "$GPGGA,103102.557,5323.0900,N,00636.1283,W,1,08,1.0,49.1,M,56.5,M,,0000*7E",
            "$GPGSA,A,3,01,11,08,19,28,32,03,18,,,,,1.7,1.0,1.3*37",
            "$GPGSV,3,1,10,08,70,154,34,11,61,270,26,01,47,260,48,22,40,062,*7E",
            "$GPGSV,3,2,10,19,40,297,46,32,39,184,32,28,28,314,43,03,11,205,41*7C",
            "$GPGSV,3,3,10,18,07,044,35,30,03,276,42*75",
            "$GPRMC,103102.557,A,5323.0900,N,00636.1283,W,000.0,308.8,101115,,,A*79",
            "$GPVTG,308.8,T,,M,000.0,N,000.0,K,A*0E"
    }; // using the data supplied in Joe's email from 10 November

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        NetHelper netHelper = new NetHelper(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** I will need the wi-fi to be constantly connected so that I can track planes while the
         *  phone is asleep.
         */
        WifiManager.WifiLock wifiLock = ((WifiManager)getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "skyRadarLock");
        wifiLock.acquire();

        netStatus = netHelper.isConnected();
        /*if(netStatus) {
            serverStatus = new checkServerTask().execute("http://sbsrv1.cs.nuim.ie:30003");

            if (serverStatus) {
                new sbsReaderTask().execute();
            } else {
                AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
                adb.setTitle("Server status")
                        .setMessage("Server unavailable. Please try later.");
            }
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
            adb.setTitle("Connectivity status");
            adb.setMessage("Your device is not connected to the Internet. Please activate your mobile data or connect to wi-fi.");
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void decodeNMEA(String sentence){
        String tag = "Decoding NMEA";
        if(sentence.startsWith("$GPRMC")) {
            String[] rmcValues = sentence.split(",");
            //TODO: Maybe try to change these doubles back into strings for
            double nmeaLatitude = Double.parseDouble(rmcValues[3]);
            double nmeaLatMin = nmeaLatitude % 100; //get minutes from latitude value
            nmeaLatitude/=100;
            if(rmcValues[4].charAt(0)=='S'){
                nmeaLatitude = -nmeaLatitude;
            }
            double nmeaLongitude = Double.parseDouble(rmcValues[5]);
            double nmeaLonMin = nmeaLongitude % 100; //get minutes from longitude value
            nmeaLongitude/=100;
            if(rmcValues[6].charAt(0)=='W'){
                nmeaLongitude = -nmeaLongitude;
            }

            Log.d(tag + ": lat", Double.toString(nmeaLatitude));
            GpsLat.setText("Latitude: " + Double.toString(nmeaLatitude));
            Log.d(tag + ": lon", Double.toString(nmeaLongitude));
            GpsLon.setText("Longitude: " + Double.toString(nmeaLongitude));
        }
    }
    class checkServerTask extends AsyncTask <String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... strUrl){
            Boolean serverStatus = false;
            try { URL url = new URL(strUrl[0]); }
            catch (IOException e) {
                e.printStackTrace();
            }
            return serverStatus;
        }

        /*@Override
        protected void onPostExecute(){

        }*/
    }
    /**
     * Version: 17 November 2015: This is supposed to read the SBS-1 data from Dr. Brown's server
     * Version: 18 November 2015: I'm getting a NetworkOnMainThread exception, I need to fix that
     */
    class sbsReaderTask extends AsyncTask <String, Void, String[]> {
        private String tag = "sbsReaderTask";

        @Override
        protected String[] doInBackground(String... params){
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            Socket socket = new Socket();
            String[] serverResponse = null;
            // try to constuct a URL for accessing the server
            try {
                //TODO: sbsrv1 is currently an "unknown protocol" - I need to fix that
                /*final String DR_BROWN_SERVER = "sbsrv1.cs.nuim.ie";
                final String DR_BROWN_PORT = "30003";
                final String FULL_SERVER_URL = DR_BROWN_SERVER + ":" + DR_BROWN_PORT;*/
                Log.d(tag, "About to try parsing a URI");
                Uri builtUri = Uri.parse("http://sbsrv1.cs.nuim.ie:30003");
                Log.d("Built URI", builtUri.toString());
                URL url = new URL(builtUri.toString());

                // open connection to the server
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    // do nothing except...
                    Log.d(tag, "InputStream is null. Closing.");
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String outputLine;
                while((outputLine = bufferedReader.readLine()) != null){
                    // Just adding a new line to be safe
                    buffer.append(outputLine + "\n");
                }
                if(buffer.length()==0){
                    // Buffer is null, don't bother parsing
                    Log.d(tag, "Buffer is null. Closing.");
                    return null;
                }
                String unsplitServerResponse = buffer.toString();
                Log.d("Server response", unsplitServerResponse);
                serverResponse = unsplitServerResponse.split(",");
                return serverResponse;
            } catch (IOException ioe) {
                Log.e(tag, ioe.toString());
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect(); // don't want to fry Dr. Brown's server with too many requests
                }
                if (bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(tag, "Error closing stream: " + e.toString());
                    }
                }
            }
            return null;
        }
    }
}

package com.example.se415017.maynoothskyradar.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.helpers.NetHelper;
import com.example.se415017.maynoothskyradar.services.SocketService;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Ciaran Cumiskey se415017 #12342236
 * @version 11 November 2015
 * Parsing the GPS data supplied by Joe in his email.
 */
public class MainActivity extends FragmentActivity {
    //TODO: move all of the UI stuff out of the Activity and into Fragments
    //TODO: fix "unable to unstantiate activity ComponentInfo" error
    //TODO: instead of using hard-coded values for the server's URL, make the user enter it
    private String strUrl = ""; //"sbsrv1.cs.nuim.ie"; moving away from hard-coded value
    private int serverPort = 30003;
    public static final String PREFS = "UserPreferences";
    public static final String SERVER_PREF = "serverAddress";
    URL url;
    public SocketService socketService;
    static final LatLng MAYNOOTH = new LatLng(53.23, -6.36);
    boolean socketServiceBound = false;
    final String TAG = "MainActivity";

    public static FragmentManager fragManager;

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
        Log.d(TAG, "activateGPS button pressed");
        for(int i = 0; i < dummyData.length; i++){
            decodeNMEA(dummyData[i]);
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
        final SharedPreferences sharedPref = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        strUrl = sharedPref.getString(SERVER_PREF, "");
        fragManager = getSupportFragmentManager();
        ButterKnife.bind(this);
        final NetHelper netHelper = new NetHelper(getApplicationContext());
        netStatus = netHelper.isConnected();
        if(netStatus) {
            if(strUrl.equalsIgnoreCase("")) {
                Log.d(TAG, "No user-saved URL detected");
                new MaterialDialog.Builder(this)
                        .title("Server")
                        .content(R.string.enter_address)
                        .positiveText("Enter")
                        .input("Server address", "", false, new MaterialDialog.InputCallback() {
                            //The "false" above doesn't allow user input when the EditText field is empty
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                Log.d(TAG, "User input = " + input.toString());
                                strUrl = input.toString();
                                SharedPreferences.Editor editor = sharedPref.edit();
                                Log.d(TAG, "onInput() Address to be saved = " + strUrl);
                                editor.putString(SERVER_PREF, strUrl);
                                //apply() works faster than commit() but commit() works immediately
                                editor.commit();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //TODO: The user's input isn't being picked up here!
                                //SharedPreferences.Editor editor = sharedPref.edit();
                                Log.d(TAG, "onPositive() invoked");
                                Log.d(TAG, "onPositive() Address to be saved = " + strUrl);
                                //editor.putString(SERVER_PREF, strUrl);
                                //editor.apply();
                            }
                        })
                        .negativeText(R.string.cancel_text)
                        .show();
            }
            try {
                strUrl = sharedPref.getString(SERVER_PREF, "");
                url = new URL("http", strUrl, serverPort, "");
                Log.d(TAG, "URL created: " + url.toString());
                Intent sockIntent = new Intent(this, SocketService.class);
                Log.d(TAG, "Intent created");
                sockIntent.putExtra("serverAddr", url.toString());
                bindService(sockIntent, mConnection, Context.BIND_AUTO_CREATE);
                /** I will need the wi-fi to be constantly connected so that I can track planes while the
                 *  phone is asleep.
                 */
                WifiManager.WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "skyRadarLock");
                wifiLock.acquire();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                new MaterialDialog.Builder(this)
                        .title("URL error")
                        .content("The URL you have entered is malformed. Please go to the settings menu and change it.")
                        .positiveText("Change URL")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                                //TODO: Take the user to the Settings menu
                            }
                        })
                        .negativeText(R.string.cancel_text)
                        .show();
            }
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.conn_unavailable_title)
                    .content(R.string.conn_unavailable_content)
                    .positiveText("Network settings")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0); // the user can return to the app by pressing the back button
                        }
                    })
                    .negativeText("Cancel")
                    .show();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        netStatus = new NetHelper(getApplicationContext()).isConnected();
        if(!socketServiceBound) {
            Intent sockIntent = new Intent(this, SocketService.class);
            if(url == null) {
                try {
                    url = new URL("http", strUrl, serverPort, "");
                } catch (MalformedURLException e) {
                    Log.e(TAG, e.toString());
                }
            }
            sockIntent.putExtra("serverAddr", url.toString());
            bindService(sockIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(socketServiceBound) {
            unbindService(mConnection);
            Log.d(TAG, "Socket service unbound from MainActivity");
            socketServiceBound = false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(socketServiceBound) {
            unbindService(mConnection);
            Log.d(TAG, "Socket service unbound from MainActivity");
        }
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

    // Necessary callbacks for service binding
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service){
            SocketService.SimpleLocalBinder binder =
                    (SocketService.SimpleLocalBinder) service;
            socketService = binder.getService();
            socketServiceBound = true;
            Log.d("MainActivity", "Socket service bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            socketServiceBound = false;
        }
    };
    /**
     * This method parses lines of NMEA data to check if they contain latitude
     * and longitude data.
     * param sentence - a line of NMEA data
     */
    public void decodeNMEA(String sentence) {
        String tag = "Decoding NMEA";
        if (sentence.startsWith("$GPRMC")) {
            String[] rmcValues = sentence.split(",");
            //TODO: Maybe try to change these doubles back into strings for
            double nmeaLatitude = Double.parseDouble(rmcValues[3]);
            double nmeaLatMin = nmeaLatitude % 100; //get minutes from latitude value
            nmeaLatitude /= 100;
            if (rmcValues[4].charAt(0) == 'S') {
                nmeaLatitude = -nmeaLatitude;
            }
            double nmeaLongitude = Double.parseDouble(rmcValues[5]);
            double nmeaLonMin = nmeaLongitude % 100; //get minutes from longitude value
            nmeaLongitude /= 100;
            if (rmcValues[6].charAt(0) == 'W') {
                nmeaLongitude = -nmeaLongitude;
            }

            Log.d(tag + ": lat", Double.toString(nmeaLatitude));
            GpsLat.setText("Latitude: " + Double.toString(nmeaLatitude));
            Log.d(tag + ": lon", Double.toString(nmeaLongitude));
            GpsLon.setText("Longitude: " + Double.toString(nmeaLongitude));
        }
    }
    //I deleted [DJKhaled]A LOT[/DJKhaled] of redundant code below
}

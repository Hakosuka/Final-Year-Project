package com.example.se415017.maynoothskyradar.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.example.se415017.maynoothskyradar.fragments.EnterURLFragment;
import com.example.se415017.maynoothskyradar.helpers.NetHelper;
import com.example.se415017.maynoothskyradar.helpers.SBSDecoder;
import com.example.se415017.maynoothskyradar.objects.Aircraft;
import com.example.se415017.maynoothskyradar.services.SocketService;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

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
    //DONE: instead of using hard-coded values for the server's URL, make the user enter it
    String strUrl = ""; //Used to be "sbsrv1.cs.nuim.ie"; moving away from hard-coded value
    int serverPort = 30003; //redundant
    public static final String PREFS = "UserPreferences";
    public static final String SERVER_PREF = "serverAddress";
    URL url;
    public SocketService socketService;
    static final LatLng MAYNOOTH = new LatLng(53.23, -6.36);
    boolean socketServiceBound = false;
    final String TAG = "MainActivity";
    ArrayList<Aircraft> aircraftArrayList;

    public static FragmentManager fragManager;

    @Bind(R.id.button_gps_activation)
    Button GpsActivationButton;
    @Bind(R.id.read_sample_log_button)
    Button ReadSampleLogButton;

    //These are the GPS coordinates of the server
    @Bind(R.id.gps_latitude)
    TextView GpsLat;
    @Bind(R.id.gps_longitude)
    TextView GpsLon;

    @Bind(R.id.server_status)
    TextView ServerStat;

    //Redundant
    @OnClick(R.id.button_gps_activation)
    public void activateGPS(View view){
        Log.d(TAG, "activateGPS button pressed");
        for(int i = 0; i < dummyData.length; i++){
            decodeNMEA(dummyData[i]);
        }
    }

    @OnClick(R.id.read_sample_log_button)
    public void readSampleLog(View view) {
        Log.d(TAG, "String from example log = " + readFromTextFile(getApplicationContext()));
    }

    boolean netStatus = false;
    boolean serverStatus = false;

    // using the data supplied in Joe's email from 10 November
    String[] dummyData = {
            "$GPGGA,103102.557,5323.0900,N,00636.1283,W,1,08,1.0,49.1,M,56.5,M,,0000*7E",
            "$GPGSA,A,3,01,11,08,19,28,32,03,18,,,,,1.7,1.0,1.3*37",
            "$GPGSV,3,1,10,08,70,154,34,11,61,270,26,01,47,260,48,22,40,062,*7E",
            "$GPGSV,3,2,10,19,40,297,46,32,39,184,32,28,28,314,43,03,11,205,41*7C",
            "$GPGSV,3,3,10,18,07,044,35,30,03,276,42*75",
            "$GPRMC,103102.557,A,5323.0900,N,00636.1283,W,000.0,308.8,101115,,,A*79",
            "$GPVTG,308.8,T,,M,000.0,N,000.0,K,A*0E"
    };
    //TODO: Research WifiLocks and determine if I need them in my app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPref = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        strUrl = sharedPref.getString(SERVER_PREF, "");

        aircraftArrayList = new ArrayList<Aircraft>();

        fragManager = getSupportFragmentManager();
        //FragmentTransaction fragmentTransaction = fragManager.beginTransaction();

        ButterKnife.bind(this);
        Fragment currentFragment;

        final NetHelper netHelper = new NetHelper(getApplicationContext());
        Log.d(TAG, "String from example log = " + readFromTextFile(getApplicationContext()));
        if(netHelper.isConnected()) {
//            if(strUrl.equalsIgnoreCase("")) {
//                //TODO: Take the user to the setup activity
//                Log.d(TAG, "No user-saved URL detected");
//                //showNoServerAddressDialog(MainActivity.this);
//                Toast.makeText(MainActivity.this, "Server address not found", Toast.LENGTH_LONG)
//                        .show();
//                Intent setUpIntent = new Intent(this, SetUpActivity.class);
//
//                //Stops the app from returning to the MainActivity if I press the back button while
//                //in the SetUpActivity
//                setUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(setUpIntent);
//            } else {
//                Log.d(TAG, "User-saved URL detected");
//                //If all else fails, use sbsrv1.cs.nuim.ie as the default string
//                strUrl = sharedPref.getString(SERVER_PREF, "sbsrv1.cs.nuim.ie");
//
//                if(!doesSocketServiceExist(SocketService.class)) {
//                    Log.d(TAG, "No existing SocketService found");
//                    try {
//                        //Now is not the time to add the port, that comes later
//                        url = new URL("http", strUrl, "");
//                        Log.d(TAG, "URL created: " + url.toString());
//                    } catch (MalformedURLException e) {
//                        Log.e(TAG, e.toString());
//                        showMalformedURLDialog(MainActivity.this);
//                    }
//                    Intent sockIntent = new Intent(this, SocketService.class);
//                    Log.d(TAG, "Intent created");
//
//                    sockIntent.putExtra("serverAddr", url.toString());
//                    //TODO: Reactivate after I've done testing with the example log
//                    //startService(sockIntent);
//                    /**
//                     * bindService kills the service upon unbinding
//                     */
//                    //bindService(sockIntent, mConnection, Context.BIND_AUTO_CREATE);
//                } else {
//                    Log.d(TAG, "Existing SocketService found");
//                }
//            }
        } else {
            showNoInternetDialog(MainActivity.this);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "MainActivity resuming");
        Log.d(TAG, "SocketService found? " + Boolean.toString(doesSocketServiceExist(SocketService.class)));
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //TODO: Define the layouts in setContentView below
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //setContentView(R.layout.landscapeView);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //setContentView(R.layout.portrait);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "Killing MainActivity");
//        if(socketServiceBound) {
//            unbindService(mConnection);
//            Log.d(TAG, "Socket service unbound from MainActivity");
//        }
//        if(doesSocketServiceExist(SocketService.class)){
//            Log.d(TAG, "Killing SocketService");
//            Intent stopSockIntent = new Intent(this, SocketService.class);
//            stopService(stopSockIntent);
//        }
        super.onDestroy();
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
            Log.d(TAG, "Socket service bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            socketServiceBound = false;
        }
    };

    /**
     * Checks if an instance of a particular service exists
     * @param serviceClass - the Service I want to look for
     * @return boolean which tells me if an instance of that Service exists
     */
    private boolean doesSocketServiceExist(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
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
    /**
     * Shows the alert dialog which notifies the user that they've entered a malformed URL.
     * @param activity
     * @return MaterialDialog
     */
    public MaterialDialog showMalformedURLDialog(final Activity activity){
        Log.d(TAG, "SocketService found? " + Boolean.toString(doesSocketServiceExist(SocketService.class)));
        return new MaterialDialog.Builder(this)
                .title(R.string.malformed_url_title)
                .content(R.string.malformed_url_content)
                .inputRange(8, 255, Color.RED)
                .positiveText("Submit")
                .input("Server address", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Log.d(TAG, "User input = " + input.toString());
                        strUrl = input.toString();
                        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(SERVER_PREF, strUrl);
                        editor.apply();
                    }
                })
                .show();
    }
    /**
     * Shows the alert dialog which notifies the user that they have no Internet connection.
     * @param activity
     * @return MaterialDialog
     */
    public MaterialDialog showNoInternetDialog(final Activity activity){
        Log.d(TAG, "SocketService found? " + Boolean.toString(doesSocketServiceExist(SocketService.class)));
        return new MaterialDialog.Builder(this)
                .title(R.string.conn_unavailable_title)
                .content(R.string.conn_unavailable_content)
                .positiveText("Network settings")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0); // the user can return to the app by pressing the back button
                    }
                })
                .negativeText("Exit app")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        System.exit(0);
                    }
                })
                .show();
    }
    /**
     * Shows the alert dialog which notifies the user that they haven't saved the address of their server.
     * Now redundant.
     * @param activity
     * @return MaterialDialog
     */
    public MaterialDialog showNoServerAddressDialog(final Activity activity){
        Log.d(TAG, "SocketService found? " + Boolean.toString(doesSocketServiceExist(SocketService.class)));
        return new MaterialDialog.Builder(this)
                .title(R.string.server_not_added)
                .content(R.string.enter_address)
                .inputRange(8, 255, Color.RED)
                .positiveText("Enter")
                .input("Server address", "", false, new MaterialDialog.InputCallback() {
                    //The "false" above doesn't allow user input when the EditText field is empty
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Log.d(TAG, "User input = " + input.toString());
                        strUrl = input.toString();
                        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        Log.d(TAG, "onInput() Address to be saved = " + strUrl);
                        editor.putString(SERVER_PREF, strUrl);
                        //apply() works faster than apply() but apply() works immediately
                        editor.apply();
                    }
                })
                .show();
    }

    public String readFromTextFile(Context context) {
        Scanner s = new Scanner(getResources().openRawResource(R.raw.samplelog));
        try {
            while (s.hasNext()) {
                String word = s.next().trim(); //remove whitespaces from the line being read
                String[] splitLine = word.split(","); //split the line from the log using the comma
                Log.d(TAG, "Line from sample log = " + word + ", has " + Integer.toString(splitLine.length) + " elements.");
                parseSBSMessage(splitLine);
            }
        } finally {
            s.close();
        }
        return s.toString();
    }

    public void parseSBSMessage(String[] sbsMessageArray){

        //sbsMessageArray[9] is the time the message was logged.
        double delay = Double.parseDouble(sbsMessageArray[9].substring(6));
        //By checking for 22 fields, we don't get thrown off by transmission messages without
        //that amount of fields
        if(aircraftArrayList != null)
            Log.d(TAG, "Number of aircraft detected: " + Integer.toString(aircraftArrayList.size()));
        else
            Log.d(TAG, "AircraftArrayList is empty");
        if(sbsMessageArray[0].equals("MSG") && sbsMessageArray.length == 22){
            //sbsMessageArray[1] is the type of transmission message
            switch(Integer.parseInt(sbsMessageArray[1])) {
                case 1:
                    Log.d(TAG, "Callsign = " + sbsMessageArray[10]);
                    break;
                case 2:
                    Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                    Log.d(TAG, "Ground speed = " + sbsMessageArray[12] + "kts");
                    Log.d(TAG, "Latitude = " + sbsMessageArray[14]);
                    Log.d(TAG, "Longitude = " + sbsMessageArray[15]);
                    break;
                case 3:
                    Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                    Log.d(TAG, "Latitude = " + sbsMessageArray[14]);
                    Log.d(TAG, "Longitude = " + sbsMessageArray[15]);
                    break;
                case 4:
                    Log.d(TAG, "Ground speed = " + sbsMessageArray[12] + "kts");
                    Log.d(TAG, "Climbing at " + sbsMessageArray[16] + "ft/min");
                    break;
                case 5:
                    Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                    break;
                case 6:
                    Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                    break;
                case 7:
                    Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                    break;
                case 8:
                    Log.d(TAG, "Is this plane on the ground? " + Boolean.toString(sbsMessageArray[21].equals("1")));
                    break;
            }
            //Checks if an aircraft with a given ICAO hex code is found in the list
            boolean hexIdentFound = false;
            if(aircraftArrayList.size() != 0){
                for(Aircraft aircraft : aircraftArrayList) {
                    //The ICAO hex code is the 5th element of the message
                    hexIdentFound = aircraft.getIcaoHexAddr().equals(sbsMessageArray[4]);
                    if (hexIdentFound) {
                        switch (Integer.parseInt(sbsMessageArray[1])) {
                            case 1:
                                aircraft.setCallsign(sbsMessageArray[10]);
                                break;
                            case 2:
                                aircraft.setAltitude(Integer.parseInt(sbsMessageArray[11]));
                                aircraft.setLatitude(Double.parseDouble(sbsMessageArray[14]));
                                aircraft.setLongitude(Double.parseDouble(sbsMessageArray[15]));
                                break;
                            case 3:
                                aircraft.setAltitude(Integer.parseInt(sbsMessageArray[11]));
                                aircraft.setLatitude(Double.parseDouble(sbsMessageArray[14]));
                                aircraft.setLongitude(Double.parseDouble(sbsMessageArray[15]));
                                break;
                            case 4:
                                //TODO: Add track
                                aircraft.setgSpeed(Integer.parseInt(sbsMessageArray[12]));
                                aircraft.setTrack(Integer.parseInt(sbsMessageArray[13]));
                                break;
                            case 5 | 6 | 7:
                                aircraft.setAltitude(Integer.parseInt(sbsMessageArray[11]));
                                break;
                            case 8:
                                break;
                        }
                        Log.d(TAG, "Aircraft status: " + aircraft.getIcaoHexAddr() + ", " +
                                aircraft.getCallsign() + ", " +
                                Integer.toString(aircraft.getAltitude()) + ", " +
                                Integer.toString(aircraft.getgSpeed()) + ", " +
                                Integer.toString(aircraft.getTrack()) + ", " +
                                Double.toString(aircraft.getLatitude()) + ", " +
                                Double.toString(aircraft.getLongitude()));
                        break; //No need to keep checking the list
                    }
                    else {
                        aircraftArrayList.add(new Aircraft(sbsMessageArray[4],
                                sbsMessageArray[10], //callsign
                                Integer.parseInt(sbsMessageArray[11]), //altitude
                                Integer.parseInt(sbsMessageArray[12]), //ground speed
                                Integer.parseInt(sbsMessageArray[13]), //track
                                Double.parseDouble(sbsMessageArray[14]), //latitude
                                Double.parseDouble(sbsMessageArray[15]))); //longitude
                    }
                }
            } else {
                //No aircraft in aircraftArrayList, now adding the first one to be discovered
                Log.d(TAG, "No aircraft found in list, now adding a new aircraft.");
                //TODO: NumberFormatException
                aircraftArrayList.add(new Aircraft(sbsMessageArray[4],
                        sbsMessageArray[10], //callsign
                        Integer.parseInt(sbsMessageArray[11]), //altitude
                        Integer.parseInt(sbsMessageArray[12]), //ground speed
                        Integer.parseInt(sbsMessageArray[13]), //track
                        Double.parseDouble(sbsMessageArray[14]), //latitude
                        Double.parseDouble(sbsMessageArray[15]))); //longitude
            }
        } else {
            Log.d(TAG, "Not a transmission message, it's a " + sbsMessageArray[0] + " instead.");
        }
    }
}

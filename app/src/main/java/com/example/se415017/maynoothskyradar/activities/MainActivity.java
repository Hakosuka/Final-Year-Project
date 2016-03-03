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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.fragments.AircraftListFragment;
import com.example.se415017.maynoothskyradar.fragments.EnterURLFragment;
import com.example.se415017.maynoothskyradar.fragments.MainMapFragment;
import com.example.se415017.maynoothskyradar.helpers.DistanceCalculator;
import com.example.se415017.maynoothskyradar.helpers.MainTabPagerAdapter;
import com.example.se415017.maynoothskyradar.helpers.NetHelper;
import com.example.se415017.maynoothskyradar.helpers.SBSDecoder;
import com.example.se415017.maynoothskyradar.objects.Aircraft;
import com.example.se415017.maynoothskyradar.services.SocketService;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Handler;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Ciaran Cumiskey se415017 #12342236
 * @version 11 November 2015
 * Parsing the GPS data supplied by Joe in his email.
 */
public class MainActivity extends AppCompatActivity {
    //TODO: (almost done) move all of the UI stuff out of the Activity and into Fragments
    //DONE: instead of using hard-coded values for the server's URL, make the user enter it
    String strUrl = ""; //Used to be "sbsrv1.cs.nuim.ie"; moved away from using hard-coded values
    int serverPort = 30003; //redundant
    public static final String PREFS = "UserPreferences";
    public static final String SERVER_PREF = "serverAddress";
    public static final String LAT_PREF = "latitude";
    public static final String LON_PREF = "longitude";
    URL url;

    public SBSDecoder sbsDecoder;
    public DistanceCalculator distCalc;

    public SocketService socketService;
    static final LatLng MAYNOOTH = new LatLng(53.23, -6.36);
    boolean socketServiceBound = false;
    final String TAG = "MainActivity";
    public ArrayList<Aircraft> aircraftArrayList;

    public static FragmentManager fragManager;

    @Bind(R.id.activity_main_tabs)
    PagerSlidingTabStrip mainTabs;
    @Bind(R.id.activity_main_pager)
    ViewPager mainPager;

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

    //DONE: Test if I can actually use ButterKnife's @Bind annotations on toolbars - I can!
    @Bind(R.id.activity_main_toolbar)
    Toolbar activityToolbar;

    @OnClick(R.id.activity_main_toolbar)
    public void toolbarClicked(View view){
        Log.d(TAG, "Toolbar clicked");
    }

    //Redundant
    @OnClick(R.id.button_gps_activation)
    public void activateGPS(View view){
        Log.d(TAG, "activateGPS button pressed");
        SharedPreferences sharedPref = getSharedPreferences(PREFS, MODE_PRIVATE);
        String latStringFromPref = Double.toString(Double.longBitsToDouble(sharedPref.getLong(LAT_PREF, 0)));
        GpsLat.setText("Latitude: " + latStringFromPref);
        String lonStringFromPref = Double.toString(Double.longBitsToDouble(sharedPref.getLong(LON_PREF, 0)));
        GpsLon.setText("Longitude: " + lonStringFromPref);
    }

    @OnClick(R.id.read_sample_log_button)
    public void readSampleLog(View view) {
        Log.d(TAG, "String from example log = " + readFromTextFile(getApplicationContext()));
    }

    boolean netStatus = false;
    boolean serverStatus = false;

    //TODO: Research WifiLocks and determine if I need them in my app (I probably don't)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(activityToolbar);
        ButterKnife.bind(this);


        final SharedPreferences sharedPref = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        strUrl = sharedPref.getString(SERVER_PREF, "");

        if(sbsDecoder == null){
            sbsDecoder = new SBSDecoder();
        }
        aircraftArrayList = new ArrayList<Aircraft>();

        fragManager = getSupportFragmentManager();
        Fragment currentFragment;
        mainPager.setAdapter(new MainTabPagerAdapter(fragManager));
        mainTabs.setViewPager(mainPager);

        final NetHelper netHelper = new NetHelper(getApplicationContext());
        Log.d(TAG, "String from example log = " + readFromTextFile(getApplicationContext()));
        if(netHelper.isConnected()) {
            if(strUrl.equalsIgnoreCase("")) {
                //TODO: Take the user to the setup activity
                Log.d(TAG, "No user-saved URL detected");
                //showNoServerAddressDialog(MainActivity.this);
                Toast.makeText(MainActivity.this, "Server address not found", Toast.LENGTH_LONG)
                        .show();
                Intent setUpIntent = new Intent(this, SetUpActivity.class);

                //Stops the app from returning to the MainActivity if I press the back button while in the SetUpActivity
                setUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(setUpIntent);
            } else {
                Log.d(TAG, "User-saved URL detected");
                //If all else fails, use sbsrv1.cs.nuim.ie as the default string
                strUrl = sharedPref.getString(SERVER_PREF, "sbsrv1.cs.nuim.ie");

                if(!doesSocketServiceExist(SocketService.class)) {
                    Log.d(TAG, "No existing SocketService found");
                    try {
                        //Now is not the time to add the port, that comes later
                        url = new URL("http", strUrl, "");
                        Log.d(TAG, "URL created: " + url.toString());
                    } catch (MalformedURLException e) {
                        Log.e(TAG, e.toString());
                        showMalformedURLDialog(MainActivity.this);
                    }
                    Intent sockIntent = new Intent(this, SocketService.class);
                    Log.d(TAG, "Intent created");

                    sockIntent.putExtra("serverAddr", url.toString());
                    //TODO: Reactivate after I've done testing with the example log
                    //startService(sockIntent);
                    /**
                     * bindService kills the service upon unbinding
                     */
                    //bindService(sockIntent, mConnection, Context.BIND_AUTO_CREATE);
                } else {
                    Log.d(TAG, "Existing SocketService found");
                }
                //fragManager.beginTransaction()
                //        .add(R.id.content_main, new AircraftListFragment()).commit();
            }
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
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_settings:
                Log.d(TAG, "Settings selected");
                return true;
            case R.id.action_about:
                Log.d(TAG, "This is where I would open a dialog to say what this app's about...IF I HAD ONE.");
                break;
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
        int count = 0;
        Scanner s = new Scanner(getResources().openRawResource(R.raw.samplelog));
        try {
            while (s.hasNext()) {
                count++;
                String word = s.next(); //.trim(); //remove whitespaces from the line being read
                String[] splitLine = word.split(","); //split the line from the log using the comma
                Log.d(TAG, "Line #" + Integer.toString(count) + " from sample log = " + word +
                        ", has " + Integer.toString(splitLine.length) + " elements.");
                //This prevents messages without the requisite amount of fields getting parsed and screwing things up.
                if(splitLine.length == 22) {
                    Aircraft newAircraft = sbsDecoder.parseSBSMessage(splitLine);
                    Log.d(TAG, "Aircraft status = " + newAircraft.toString());
                    sbsDecoder.searchThroughAircraftList(aircraftArrayList, newAircraft, Integer.parseInt(splitLine[1]));
                } else {
                    Log.d(TAG, "Insufficient amount of fields in message from " + splitLine[4]);
                }
            }
        } finally {
            Log.d(TAG, "Finished reading file, " + Integer.toString(aircraftArrayList.size()) +
                    " aircraft detected.");
            s.close();
            ArrayList<Aircraft> aircraftListToCompare = new ArrayList<Aircraft>();
            for(Aircraft a : aircraftArrayList){
                Log.d(TAG, "Detected: " + a.toString());
                if(a.altitude != null && a.longitude != null && a.latitude != null){
                    Log.d(TAG, "Adding aircraft " + a.icaoHexAddr);
                    aircraftListToCompare.add(a);
                }
            }
            for(Aircraft a : aircraftListToCompare){
                Log.d(TAG, "Valid aircraft detected: " + a.toString());
                Aircraft nearestAircraft = new Aircraft();
                double lowest2DDist = 99999.9;
                double lowest3DDist = 99999.9;
                for(Aircraft b : aircraftListToCompare){
                    Log.d(TAG, "Comparing against aircraft: " + b.toString());
                    //Stops us from comparing the same Aircraft against itself
                    if(!a.equals(b)){
                        double twoDDist = distCalc.twoDDistanceBetweenAircraft(a, b);
                        double threeDDist = distCalc.threeDDistanceBetweenAircraft(a, b);
                        Log.d(TAG, "2D distance between " + a.icaoHexAddr + " and " + b.icaoHexAddr + "= " + Double.toString(twoDDist) + "km");
                        Log.d(TAG, "3D distance between " + a.icaoHexAddr + " and " + b.icaoHexAddr + "= " + Double.toString(threeDDist) + "km");
                        if(twoDDist < lowest2DDist && threeDDist < lowest3DDist)
                            nearestAircraft = b;
                    }
                }
                Log.d(TAG, "The closest aircraft to " + a.icaoHexAddr + " is " + nearestAircraft.toString());
            }
        }
        return s.toString();
    }

    class IncomingHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg){
            if(msg.what == SocketService.MESSAGE){
                Log.d(TAG, "Message from SocketService = " + msg.getData().getString("sbsMessage"));
            } else {
                Log.d(TAG, "Invalid message from SocketService");
            }
        }
    }
/**
 * The below code has all been copied into SBSDecoder for easier re-usability
 */

//    public void parseSBSMessage(String[] sbsMessageArray){
//        Aircraft aircraftToAddOrModify = new Aircraft();
//        Log.d(TAG, "Number of aircraft detected: " + Integer.toString(aircraftArrayList.size()));
//        //By checking for 22 fields, we don't get thrown off by transmission messages without that amount of fields
//        //if(sbsMessageArray.length == 22) {
//        //The above is redundant thanks to the array length checking done in readFromTextFile()
//        //sbsMessageArray[1] is the type of transmission message
//        aircraftToAddOrModify.icaoHexAddr = sbsMessageArray[4];
//        switch (sbsMessageArray[0]) {
//            case "MSG":
//                switch (Integer.parseInt(sbsMessageArray[1])) {
//                    case 1:
//                        Log.d(TAG, "Callsign = " + sbsMessageArray[10]);
//                        aircraftToAddOrModify.callsign = sbsMessageArray[10];
//                        break;
//                    case 2:
//                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
//                        Log.d(TAG, "Ground speed = " + sbsMessageArray[12] + "kts");
//                        Log.d(TAG, "Track = " + sbsMessageArray + "\u00b0");
//                        Log.d(TAG, "Latitude = " + sbsMessageArray[14]);
//                        Log.d(TAG, "Longitude = " + sbsMessageArray[15]);
//                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
//                        aircraftToAddOrModify.gSpeed = Integer.parseInt(sbsMessageArray[12]);
//                        aircraftToAddOrModify.track = Integer.parseInt(sbsMessageArray[13]);
//                        aircraftToAddOrModify.latitude = Double.parseDouble(sbsMessageArray[14]);
//                        aircraftToAddOrModify.longitude = Double.parseDouble(sbsMessageArray[15]);
//                        break;
//                    case 3:
//                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
//                        Log.d(TAG, "Latitude = " + sbsMessageArray[14]);
//                        Log.d(TAG, "Longitude = " + sbsMessageArray[15]);
//                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
//                        aircraftToAddOrModify.latitude = Double.parseDouble(sbsMessageArray[14]);
//                        aircraftToAddOrModify.longitude = Double.parseDouble(sbsMessageArray[15]);
//                        break;
//                    case 4:
//                        Log.d(TAG, "Ground speed = " + sbsMessageArray[12] + "kts");
//                        Log.d(TAG, "Track = " + sbsMessageArray[13] + "\u00b0");
//                        Log.d(TAG, "Climbing at " + sbsMessageArray[16] + "ft/min");
//                        aircraftToAddOrModify.gSpeed = Integer.parseInt(sbsMessageArray[12]);
//                        aircraftToAddOrModify.track = Integer.parseInt(sbsMessageArray[13]);
//                        break;
//                    //"OR" operators in switch statements was a bad idea
//                    case 5:
//                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
//                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
//                        break;
//                    case 6:
//                        Log.d(TAG, "Squawk = " + sbsMessageArray[17]);
//                        break;
//                    case 7:
//                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
//                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
//                        break;
//                    case 8:
//                        //Log.d(TAG, "Is " + sbsMessageArray[4] + " on the ground? " + Boolean.toString(sbsMessageArray[21].equals("1")));
//                        break;
//                }
//                break;
//            default:
//                break;
//        }
//        Log.d(TAG, "Aircraft status = "+ sbsMessageArray[1] + ", " + aircraftToAddOrModify.toString());
//        searchThroughAircraftArrayList(aircraftToAddOrModify, Integer.parseInt(sbsMessageArray[1]));
//    }
//
//    /**
//     *
//     * @param aircraftToSearchFor The Aircraft object that we're searching through the ArrayList for
//     * @param transMessageType The type of transmission message received from an aircraft
//     */
//    public void searchThroughAircraftArrayList(Aircraft aircraftToSearchFor, int transMessageType) {
//        //Checks if an aircraft with a given ICAO hex code is found in the list
//        boolean hexIdentFound = false;
//
//        //There's no point iterating through an empty list.
//        if (aircraftArrayList.size() > 0) {
//            //foreach loops were causing ConcurrentModificationExceptions
//            for (int i = 0; i < aircraftArrayList.size(); i++) {
//                //This Aircraft object is just something to compare newly-discovered ones against
//                Aircraft aircraftToCompare = aircraftArrayList.get(i);
//                hexIdentFound = aircraftToCompare.icaoHexAddr.equals(aircraftToSearchFor.icaoHexAddr);
//                if (hexIdentFound) {
//                    Log.d(TAG, "Updating " + aircraftToSearchFor.icaoHexAddr + ", current status: "
//                            + aircraftToCompare.toString());
//                    /**
//                     * Some fields aren't included in different transmission message types. So,
//                     * if we receive subsequent messages from an aircraft, we fill in missing
//                     * fields in messages using its last known values for each field.
//                     *
//                     * For example, MSG,1 just has the callsign of an aircraft. We get its
//                     * altitude, ground speed, track, latitude & longitude from the
//                     * corresponding Aircraft object.
//                     */
//                    switch (transMessageType){
//                        case 1:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 1");
//                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
//                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
//                            aircraftToSearchFor.track = aircraftToCompare.track;
//                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
//                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
//                            break;
//                        case 2:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 2");
//                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
//                            aircraftToSearchFor.track = aircraftToCompare.track;
//                            break;
//                        case 3:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 3");
//                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
//                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
//                            aircraftToSearchFor.track = aircraftToCompare.track;
//                            break;
//                        case 4:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 4");
//                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
//                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
//                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
//                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
//                            break;
//                        case 5:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 5");
//                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
//                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
//                            aircraftToSearchFor.track = aircraftToCompare.track;
//                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
//                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
//                            break;
//                        case 6:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 6");
//                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
//                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
//                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
//                            aircraftToSearchFor.track = aircraftToCompare.track;
//                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
//                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
//                            break;
//                        case 7:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 7");
//                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
//                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
//                            aircraftToSearchFor.track = aircraftToCompare.track;
//                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
//                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
//                            break;
//                        case 8:
//                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 8");
//                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
//                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
//                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
//                            aircraftToSearchFor.track = aircraftToCompare.track;
//                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
//                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
//                            break;
//                    }
//                    Log.d(TAG, "Modified aircraft status: " + aircraftToSearchFor.toString());
//                    aircraftArrayList.set(i, aircraftToSearchFor); //Add the modified Aircraft object to the ArrayList
//                    break; //No need to keep checking the list
//                }
//            }
//            //We've iterated through the entire Aircraft list and haven't found aircraftToSearchFor,
//            //so we'll add it to the list.
//            if (!hexIdentFound) {
//                Log.d(TAG, "Adding new aircraft to list: " + aircraftToSearchFor.toString());
//                aircraftArrayList.add(aircraftToSearchFor);
//            }
//        } else {
//            //No aircraft in aircraftArrayList, now adding the first one to be discovered
//            Log.d(TAG, "No aircraft found in list, now adding a new aircraft.");
//            aircraftArrayList.add(aircraftToSearchFor);
//        }
//    }

    /**
     * This method parses lines of NMEA data to check if they contain latitude
     * and longitude data.
     * param sentence - a line of NMEA data
     *
     * 2 March 2016 - Now redundant.
     */
//    public void decodeNMEA(String sentence) {
//        String tag = "Decoding NMEA";
//        if (sentence.startsWith("$GPRMC")) {
//            String[] rmcValues = sentence.split(",");
//            double nmeaLatitude = Double.parseDouble(rmcValues[3]);
//            double nmeaLatMin = nmeaLatitude % 100; //get minutes from latitude value
//            nmeaLatitude /= 100;
//            if (rmcValues[4].charAt(0) == 'S') {
//                nmeaLatitude = -nmeaLatitude;
//            }
//            double nmeaLongitude = Double.parseDouble(rmcValues[5]);
//            double nmeaLonMin = nmeaLongitude % 100; //get minutes from longitude value
//            nmeaLongitude /= 100;
//            if (rmcValues[6].charAt(0) == 'W') {
//                nmeaLongitude = -nmeaLongitude;
//            }
//
//            Log.d(tag + ": lat", Double.toString(nmeaLatitude));
//            GpsLat.setText("Latitude: " + Double.toString(nmeaLatitude));
//            Log.d(tag + ": lon", Double.toString(nmeaLongitude));
//            GpsLon.setText("Longitude: " + Double.toString(nmeaLongitude));
//        }
//    }
    // using the data supplied in Joe's email from 10 November
//    String[] dummyData = {
//            "$GPGGA,103102.557,5323.0900,N,00636.1283,W,1,08,1.0,49.1,M,56.5,M,,0000*7E",
//            "$GPGSA,A,3,01,11,08,19,28,32,03,18,,,,,1.7,1.0,1.3*37",
//            "$GPGSV,3,1,10,08,70,154,34,11,61,270,26,01,47,260,48,22,40,062,*7E",
//            "$GPGSV,3,2,10,19,40,297,46,32,39,184,32,28,28,314,43,03,11,205,41*7C",
//            "$GPGSV,3,3,10,18,07,044,35,30,03,276,42*75",
//            "$GPRMC,103102.557,A,5323.0900,N,00636.1283,W,000.0,308.8,101115,,,A*79",
//            "$GPVTG,308.8,T,,M,000.0,N,000.0,K,A*0E"
//    };
}

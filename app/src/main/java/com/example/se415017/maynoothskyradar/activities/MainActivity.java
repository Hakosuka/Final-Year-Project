package com.example.se415017.maynoothskyradar.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.fragments.AircraftListFragment;
import com.example.se415017.maynoothskyradar.fragments.MainMapFragment;
import com.example.se415017.maynoothskyradar.helpers.DistanceCalculator;
import com.example.se415017.maynoothskyradar.helpers.NetHelper;
import com.example.se415017.maynoothskyradar.helpers.SBSDecoder;
import com.example.se415017.maynoothskyradar.services.TextFileReaderService;
import com.example.se415017.maynoothskyradar.objects.Aircraft;
import com.example.se415017.maynoothskyradar.services.SocketService;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Ciaran Cumiskey se415017 #12342236
 * @version 11 November 2015
 * Parsing the GPS data supplied by Joe in his email.
 */
public class MainActivity extends AppCompatActivity implements
        MainMapFragment.OnFragmentInteractionListener,
        AircraftListFragment.OnListFragmentInteractionListener {
    //DONE: move all of the UI stuff out of the Activity and into Fragments
    //DONE: instead of using hard-coded values for the server's URL, make the user enter it
    String strUrl = ""; //Used to be "sbsrv1.cs.nuim.ie"; moved away from using hard-coded values
    int serverPort = 30003; //redundant
    public static final String PREFS = "UserPreferences";
    public static final String SERVER_PREF = "serverAddress";
    public static final String LAT_PREF = "latitude";
    public static final String LON_PREF = "longitude";
    public static final String SBS_MSG = "sbsMessage";
    public static final String TFRS_MSG = "sbsSampleLog";
    URL url;

    public SBSDecoder sbsDecoder;
    public DistanceCalculator distCalc;

    Messenger sMsgr = null;
    Messenger tfrsMsgr = null;

    final Messenger sockMessenger = new Messenger(new IncomingHandler());
    final Messenger tfrsMessenger = new Messenger(new IncomingHandler());

    //static final LatLng MAYNOOTH = new LatLng(53.23, -6.36);
    boolean socketServiceBound = false;
    boolean tfrServiceBound = false;
    final String TAG = "MainActivity";
    public ArrayList<Aircraft> aircraftArrayList;

    public static FragmentManager fragManager;
    private Fragment currentFrag;
    private MainMapFragment mainMapFrag;
    private AircraftListFragment aircraftListFrag;
    private MainTabPagerAdapter adapter;

    @Bind(R.id.activity_main_tabs)
    PagerSlidingTabStrip mainTabs;
    @Bind(R.id.activity_main_pager)
    ViewPager mainPager;

    //DONE: Test if I can actually use ButterKnife's @Bind annotations on toolbars - I can!
    //Redundant as of 15 March
    //DONE: Try to show the SlidingTabPagerStrip under the Toolbar
//    @Bind(R.id.activity_main_toolbar)
//    Toolbar activityToolbar;
//
//    @OnClick(R.id.activity_main_toolbar)
//    public void toolbarClicked(View view){
//        Log.d(TAG, "Toolbar clicked");
//    }


    boolean netStatus = false;
    boolean serverStatus = false;
    boolean resuming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setSupportActionBar(activityToolbar);
        cleanUpActionBar();
        ButterKnife.bind(this); //DONE: Unable to bind views - I'd forgot to comment-out the Buttons and TextViews.

        final SharedPreferences sharedPref = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        strUrl = sharedPref.getString(SERVER_PREF, "");

        if(sbsDecoder == null){
            sbsDecoder = new SBSDecoder();
        }
        if(distCalc == null){
            distCalc = new DistanceCalculator();
        }
//        if(textFileReaderService == null){
//            textFileReaderService = new TextFileReaderService();
//        }
        if(aircraftArrayList == null) {
            aircraftArrayList = new ArrayList<Aircraft>();
        }
        if(savedInstanceState != null) {
            if (savedInstanceState.getSerializable("aircraftArrayList") instanceof ArrayList<?>) {
                ArrayList<?> unknownTypeList = (ArrayList<?>) savedInstanceState
                        .getSerializable("aircraftArrayList");
                if (unknownTypeList != null && unknownTypeList.size() > 0) {
                    for (int i = 0; i < unknownTypeList.size(); i++) {
                        Object unknownTypeObject = unknownTypeList.get(i);
                        if (unknownTypeObject instanceof Aircraft) {
                            aircraftArrayList.add((Aircraft) unknownTypeObject);
                        }
                    }
                }
            }
            resuming = savedInstanceState.getBoolean("resuming", false);
        } else {
            Log.d(TAG, "savedInstanceState was null");
        }
        final NetHelper netHelper = new NetHelper(getApplicationContext());
        //Log.d(TAG, "String from example log = " + readFromTextFile(getApplicationContext()));
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
                try {
                    url = new URL("https", strUrl, 30003, "");
                    Log.d(TAG, "URL created: " + url.toString());
                } catch (MalformedURLException e) {
                    Log.e(TAG, e.toString());
                    showMalformedURLDialog(MainActivity.this);
                }
                if(!doesThisServiceExist(SocketService.class)) {
                    Log.d(TAG, "No existing SocketService found");
                    Log.d(TAG, "Intent created");
                    //DONE: Reactivate after I've done testing with the example log
                    isSocketServiceRunning(url.toString());
                } else {
                    Log.d(TAG, "Existing SocketService found");
                    bindToSocketService(url.toString());
                } if (!doesThisServiceExist(TextFileReaderService.class)) {
                    Log.d(TAG, "No existing TextFileReaderService found");
                    isTFRServiceRunning();
                } else {
                    Log.d(TAG, "Existing TextFileReaderService found");
                    //When this wasn't set to true, when this Activity was being re-created
                    bindToTFRService(true);
                }
            }
        } else {
            showNoInternetDialog(MainActivity.this);
        }
        fragManager = getSupportFragmentManager();
        adapter = new MainTabPagerAdapter(fragManager, aircraftArrayList, false);
        mainPager.setAdapter(adapter);
        mainTabs.setViewPager(mainPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "MainActivity resuming");
        Log.d(TAG, "SocketService found? " + Boolean.toString(doesThisServiceExist(SocketService.class)));
        Log.d(TAG, "TextFileReaderService found? " + Boolean.toString(doesThisServiceExist(TextFileReaderService.class)));
        isTFRServiceRunning();
        isSocketServiceRunning(url.toString());
        if(adapter != null) {
            adapter.activityResuming = true;
            adapter.updateAircraftArrayList(aircraftArrayList);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bindToTFRService(false);
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
        if(socketServiceBound) {
            try {
                unbindFromSocketService();
            } catch (Throwable t) {
                Log.e(TAG, "Failed to unbind from SocketService");
            }
        }
        if(tfrServiceBound) {
            try {
                unbindFromTFRService();
            } catch (Throwable t) {
                Log.e(TAG, "Failed to unbind from TextFileReaderService");
            }
        }
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
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                //TODO: Work on SettingsActivity
                startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                showAboutDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        Log.d(TAG, "Saving instance state");
        outState.putSerializable("aircraftArrayList", aircraftArrayList);
        outState.putBoolean("resuming", true);
        super.onSaveInstanceState(outState);
    }

    //The two ServiceConnections below are necessary callbacks for service binding
    private ServiceConnection sockConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service){
            Log.d(TAG, "onServiceConnected to " + className + "-" + service.toString());
            sMsgr = new Messenger(service);
            //I was getting ClassCastException errors when trying to do this
            //SocketService.SimpleLocalBinder binder = (SocketService.SimpleLocalBinder) service;
            //socketService = binder.getService();
            try {
                Message msg = Message.obtain(null, SocketService.MSG_REG_CLIENT);
                msg.replyTo = sockMessenger;
                sMsgr.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
            socketServiceBound = true;
            Log.d(TAG, "Socket service bound");
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            socketServiceBound = false;
            sMsgr = null;
        }
    };

    private ServiceConnection tfrsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected to " + name + "-" +service.toString());
            tfrsMsgr = new Messenger(service);
            try {
                Message firstMsg = Message.obtain(null, TextFileReaderService.MSG_REG_CLIENT);
                Message secondMsg = Message.obtain(null, TextFileReaderService.MSG_START_READING);
                firstMsg.replyTo = tfrsMessenger;
                secondMsg.replyTo = tfrsMessenger;
                Log.d(TAG, "Replying to: " + firstMsg.replyTo.toString());
                tfrsMsgr.send(firstMsg);
                tfrsMsgr.send(secondMsg);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tfrServiceBound = false;
            tfrsMsgr = null;
        }
    };

    private void cleanUpActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setElevation(0);
            actionBar.setTitle(getTitle());
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            actionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.left_48));
        }
    }

    /**
     * Checks if an instance of a particular service exists
     * @param serviceClass - the Service I want to look for
     * @return boolean which tells me if an instance of that Service exists
     */
    private boolean doesThisServiceExist(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void isSocketServiceRunning(String urlToConnectTo){
        Log.d(TAG, "Checking if SocketService is running");
        if(SocketService.isRunning()){
            bindToSocketService(urlToConnectTo);
        } else {
            Intent sockIntent = new Intent(this, SocketService.class);
            sockIntent.putExtra("serverAddr", urlToConnectTo);
            startService(sockIntent);
            bindToSocketService(urlToConnectTo);
        }
    }
    private void isTFRServiceRunning(){
        Log.d(TAG, "Checking if TextFileReaderService is running");
        if(TextFileReaderService.isRunning()){
            bindToTFRService(true);
            aircraftArrayList = TextFileReaderService.getAircraftArrayList();
        } else {
            //Intent tfrsIntent = new Intent(this, TextFileReaderService.class);
            //tfrsIntent.putExtra("MESSENGER", new Messenger(tfrsMessenger, new TextFileReaderService.TextFileBinder()));
            startService(new Intent(this, TextFileReaderService.class));
            bindToTFRService(false);
        }
    }

    //These next 2 methods bind to SocketService and TextFileReaderService
    void bindToSocketService(String urlToConnectTo){
        Log.d(TAG, "Binding to SocketService");
        Intent sockBindingIntent = new Intent(this, SocketService.class);
        sockBindingIntent.putExtra("serverAddr", urlToConnectTo);
        bindService(sockBindingIntent, sockConnection, Context.BIND_AUTO_CREATE);
        socketServiceBound = true;
    }
    void bindToTFRService(boolean restartRead){
        Log.d(TAG, "Binding to TextFileReaderService");
        Intent tfrsIntent = new Intent(this, TextFileReaderService.class);
        tfrsIntent.putExtra("origin", "MainActivity");
        tfrsIntent.putExtra("doIRestart", restartRead);
        bindService(new Intent(this, TextFileReaderService.class), tfrsConnection, Context.BIND_AUTO_CREATE);
        tfrServiceBound = true;
    }

    //These next 2 methods unbind from SocketService and TextFileReaderService
    void unbindFromSocketService(){
        if(socketServiceBound) {
            if(sMsgr != null) {
                try {
                    Message msg = Message.obtain(null, SocketService.MSG_UNREG_CLIENT);
                    msg.replyTo = sockMessenger;
                    sMsgr.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
            unbindService(sockConnection);
            socketServiceBound = false;
        }
    }
    void unbindFromTFRService(){
        if(tfrServiceBound) {
            if(tfrsMsgr != null) {
                try {
                    Message msg = Message.obtain(null, TextFileReaderService.MSG_UNREG_CLIENT);
                    msg.replyTo = tfrsMessenger;
                    tfrsMsgr.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
            unbindService(tfrsConnection);
            tfrServiceBound = false;
        }
    }
    /**
     * Shows the alert dialog which notifies the user that they've entered a malformed URL.
     * @param activity
     * @return MaterialDialog
     */
    public MaterialDialog showMalformedURLDialog(final Activity activity){
        Log.d(TAG, "SocketService found? " + Boolean.toString(doesThisServiceExist(SocketService.class)));
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
        Log.d(TAG, "SocketService found? " + Boolean.toString(doesThisServiceExist(SocketService.class)));
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
     * Just a dialog that gives some basic details about the app.
     * @return MaterialDialog which gives some basic details about the app.
     */
    public MaterialDialog showAboutDialog(){
        return new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content("Developed by Ciaran Cumiskey at Maynooth University.\n\n" +
                        "This is MaynoothSkyRadar. This app streams data from a server which uses " +
                        "dump1090 to receive messages from nearby aircraft and then forwards them " +
                        "on to clients.")
                .show();
    }

    /**
     * Returns the tag of a Fragment so that I can find it.
     * @param viewPagerId The ID of the ViewPager
     * @param position The position in the ViewPager of the Fragment we want to find
     * @return
     */
    private String getFragmentTag(int viewPagerId, int position) {
        return "android:switcher" + viewPagerId + ":" + position;
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //Leaving this method empty is OK
        Log.d(TAG, "Interacted with Fragment:" + uri.toString());
    }

    @Override
    public void onListFragmentInteraction(Aircraft dummyItem){
        Log.d(TAG, "Interacted with ListFragment");
    }

    /**
     * Sends a message to a service to get it to send messages to this activity.
     * @param msgCode
     */
    private void sendMessageToService(int msgCode) {
        if(socketServiceBound) {
            try {
                Message msg = Message.obtain(null, SocketService.MESSAGE, msgCode);
                msg.replyTo = sockMessenger;
                sockMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        if(tfrServiceBound) {
            try {
                Message msg = Message.obtain(null, TextFileReaderService.MSG_START_READING, msgCode);
                msg.replyTo = tfrsMessenger;
                tfrsMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private ArrayList<Aircraft> findNearestAircraft(ArrayList<Aircraft> aircraftListToCompare) {
        for(Aircraft a : aircraftArrayList) {
            double lowest2DDist = 99999.0;
            double lowest3DDist = 99999.0;
            for(Aircraft b : aircraftArrayList) {
                if(!a.equals(b) && b.latitude != null && b.longitude != null) {
                    double twoDDist = distCalc.twoDDistanceBetweenAircraft(a, b);
                    double threeDDist = distCalc.threeDDistanceBetweenAircraft(a, b);
                    if (twoDDist < lowest2DDist && threeDDist < lowest3DDist) {
                        lowest2DDist = twoDDist;
                        lowest3DDist = threeDDist;
                        a.nearestNeighbour = b;
                        b.nearestNeighbour = a;
                        a.twoDDistToNN = twoDDist;
                        b.twoDDistToNN = twoDDist;
                        a.threeDDistToNN = threeDDist;
                        b.threeDDistToNN = threeDDist;
                    }
                }
            }
        }
        return aircraftArrayList;
    }

    @SuppressLint("HandlerLeak")
    class IncomingHandler extends android.os.Handler {
        @Override
        public void handleMessage(final Message msg){
            if(msg.what == SocketService.MESSAGE){
                String sockResponse = msg.getData().getString(SBS_MSG);
                Log.d(TAG, "Message from SocketService = " + sockResponse);
                if(sockResponse != null) {
                    String[] splitMessage = sockResponse.split(",");
                    Aircraft newAircraft = sbsDecoder.parseSBSMessage(splitMessage);
                    aircraftArrayList = sbsDecoder.searchThroughAircraftList(aircraftArrayList,
                            newAircraft, Integer.parseInt(splitMessage[1]));
                    aircraftArrayList = findNearestAircraft(aircraftArrayList);
                    //The data has changed, the below method updates the adapter's associated views
                    adapter.updateAircraftArrayList(aircraftArrayList);
                }
            } else if (msg.what == TextFileReaderService.MSG_START_READING) {
                String tfrsResponse = msg.getData().getString("sbsSampleLog");
                Log.d(TAG, "Message from TextFileReaderService = " + tfrsResponse);
                if(tfrsResponse != null) {
                    String[] splitMessage = tfrsResponse.split(",");
                    Aircraft newAircraft = sbsDecoder.parseSBSMessage(splitMessage);
                    Log.d(TAG, "New aircraft = " + newAircraft.toString());
                    aircraftArrayList = sbsDecoder.searchThroughAircraftList(aircraftArrayList,
                            newAircraft, Integer.parseInt(splitMessage[1]));
                    aircraftArrayList = findNearestAircraft(aircraftArrayList);
                    adapter.updateAircraftArrayList(aircraftArrayList);
                }
            } else {
                Log.d(TAG, "Invalid message from SocketService");
            }
        }
    }

    /**
     * 11 March 2016 - moved MainTabPagerAdapter inside MainActivity because, well, it's my app's
     * main activity.
     * 27 March 2016 - tested out changing MainTabPagerAdapter from a FragmentStatePagerAdapter to a
     * FragmentPagerAdapter, as the former only keeps a reference to ONE Fragment at a time, but the
     * latter can keep a reference to as many Fragments as it can handle. This helps with smoother
     * animation when switching fragments.
     * As my app only has two Fragments, there's not much to lose from switching to a
     * FragmentPagerAdapter.
     *               - I've misunderstood getItem() in PagerAdapters, they keep a reference to the
     *               current Fragment and the ones next to it in the PagerAdapter.
     */
    private class MainTabPagerAdapter extends FragmentPagerAdapter {
        final String[] TAB_TITLES = {"Map", "List of planes"};
        final String AIR_KEY = "aircraftKey";
        FragmentManager fragMgr;
        public Bundle bundle;
        ArrayList<Aircraft> aircraftArrayList;
        boolean activityResuming = false;

        public MainTabPagerAdapter(FragmentManager fm, ArrayList<Aircraft> aircraftArrayList, boolean activityResuming) {
            super(fm);
            fragMgr = fm;
            this.aircraftArrayList = aircraftArrayList;
            this.activityResuming = activityResuming;
        }

        /**
         * Updates the AircraftArrayList held in both of these Fragments.
         * @param aircraftArrayList
         */
        public void updateAircraftArrayList(ArrayList<Aircraft> aircraftArrayList) {
            Log.d(TAG, "Updating aircraft ArrayList");
            Log.d(TAG, "Is activity resuming? " + Boolean.toString(activityResuming));
            this.aircraftArrayList = aircraftArrayList;
            notifyDataSetChanged();
            if (mainMapFrag != null) {
                mainMapFrag.updateAircrafts(aircraftArrayList, activityResuming);
            } else {
                Log.d(TAG, "No MainMapFragment found");
            } if (aircraftListFrag != null) {
                aircraftListFrag.updateDataset(aircraftArrayList);
            } else {
                Log.d(TAG, "No AircraftListFragment found");
            }
        }

        @Override
        public CharSequence getPageTitle(int position){
            return TAB_TITLES[position];
        }

        @Override
        public int getCount() { return TAB_TITLES.length; }

        /**
         * getItem() is really quite a misnomer, it isn't called whenever the user swipes to a new
         * page, but instead when the new Fragments are being instantiated. DON'T save references
         * to Fragments here.
         * @param position
         * @return Fragment
         */
        @Override
        public Fragment getItem(int position){
            switch(position){
                case 0:
                    Log.d(TAG, "Initialised MainMapFragment");
                    return MainMapFragment.newInstance(aircraftArrayList);
                case 1:
                    Log.d(TAG, "Initialised AircraftListFragment");
                    return AircraftListFragment.newInstance(1, aircraftArrayList);
            }
            return null;
        }

        /**
         * This is where the Fragments are actually instantiated, thus it's safe to save
         * references to Fragments here.
         * @param container
         * @param position
         * @return createdFragment - the newly-created Fragment
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment)super.instantiateItem(container, position);
            switch(position) {
                case 0:
                    mainMapFrag = (MainMapFragment) createdFragment;
                    break;
                case 1:
                    aircraftListFrag = (AircraftListFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }
    }
    //DONE: Get TextFileReaderService working so that this stuff becomes irrelevant
//    public String readFromTextFile(Context context) {
//        int count = 0;
//        Scanner s = new Scanner(getResources().openRawResource(R.raw.samplelog));
//        try {
//            while (s.hasNext()) {
//                count++;
//                String word = s.next(); //.trim(); //remove whitespaces from the line being read
//                String[] splitLine = word.split(","); //split the line from the log using the comma
//                Log.d(TAG, "Line #" + Integer.toString(count) + " from sample log = " + word +
//                        ", has " + Integer.toString(splitLine.length) + " elements.");
//                //This prevents messages without the requisite amount of fields getting parsed and screwing things up.
//                if(splitLine.length == 22) {
//                    Aircraft newAircraft = sbsDecoder.parseSBSMessage(splitLine);
//                    Log.d(TAG, "Aircraft status = " + newAircraft.toString());
//                    sbsDecoder.searchThroughAircraftList(aircraftArrayList, newAircraft, Integer.parseInt(splitLine[1]));
//                } else {
//                    Log.d(TAG, "Insufficient amount of fields in message from " + splitLine[4]);
//                }
//            }
//        } finally {
//            Log.d(TAG, "Finished reading file, " + Integer.toString(aircraftArrayList.size()) +
//                    " aircraft detected.");
//            s.close();
//            ArrayList<Aircraft> aircraftListToCompare = new ArrayList<Aircraft>();
//            for(Aircraft a : aircraftArrayList){
//                Log.d(TAG, "Detected: " + a.toString());
//                if(a.altitude != null && a.longitude != null && a.latitude != null){
//                    Log.d(TAG, "Adding aircraft " + a.icaoHexAddr);
//                    aircraftListToCompare.add(a);
//                }
//            }
//            for(Aircraft a : aircraftListToCompare){
//                Log.d(TAG, "Valid aircraft detected: " + a.toString());
//                Aircraft nearestAircraft = new Aircraft();
//                double lowest2DDist = 99999.9;
//                double lowest3DDist = 99999.9;
//                for(Aircraft b : aircraftListToCompare){
//                    Log.d(TAG, "Comparing against aircraft: " + b.toString());
//                    //Stops us from comparing the same Aircraft against itself
//                    if(!a.equals(b)){
//                        double twoDDist = distCalc.twoDDistanceBetweenAircraft(a, b);
//                        double threeDDist = distCalc.threeDDistanceBetweenAircraft(a, b);
//                        Log.d(TAG, "2D distance between " + a.icaoHexAddr + " and " + b.icaoHexAddr + "= " + Double.toString(twoDDist) + "km");
//                        Log.d(TAG, "3D distance between " + a.icaoHexAddr + " and " + b.icaoHexAddr + "= " + Double.toString(threeDDist) + "km");
//                        if(twoDDist < lowest2DDist && threeDDist < lowest3DDist)
//                            nearestAircraft = b;
//                    }
//                }
//                Log.d(TAG, "The closest aircraft to " + a.icaoHexAddr + " is " + nearestAircraft.toString());
//            }
//        }
//        return s.toString();
//    }
}
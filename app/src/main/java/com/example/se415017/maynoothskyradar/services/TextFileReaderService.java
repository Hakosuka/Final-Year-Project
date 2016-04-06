package com.example.se415017.maynoothskyradar.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.helpers.DistanceCalculator;
import com.example.se415017.maynoothskyradar.helpers.SBSDecoder;
import com.example.se415017.maynoothskyradar.objects.Aircraft;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by se415017 on 03/03/2016.
 *
 * Another attempt at taking code out of another class and moving it to its own class for easy
 * re-use.
 */
public class TextFileReaderService extends Service {
    private String TAG = getClass().getSimpleName();
    SBSDecoder sbsDecoder;
    DistanceCalculator distCalc;
    double delay = 0.0; //Simulates the time between each message being received.

    public static final int MSG_START_READING = 2; //SocketService's MESSAGE value is 1
    public static final int MSG_REG_CLIENT = 4;
    public static final int MSG_UNREG_CLIENT = 6;

    public static final String AIR_KEY = "aircraftKey";

    public static boolean isRunning = false;
    public static boolean finishedReading = false; //This stops the text file from being read again upon the Activity re-binding to this Service
    int lastSentenceRead = 0; //The last line of the text file which has been read
    int lastSentenceReadBeforeUnbinding = 0;

    Intent bindingIntent;
    //Allows for communication with MainActivity
    IBinder myBinder = new TextFileBinder();
    final Messenger messenger = new Messenger(new IncomingHandler());
    ArrayList<Messenger> messageClientList = new ArrayList<>();
    static ArrayList<Aircraft> aircraftArrayList = new ArrayList<>();
    Handler delaySimulator = new Handler();

    //Basic constructor class
    public TextFileReaderService(){
        sbsDecoder = new SBSDecoder();
        distCalc = new DistanceCalculator();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TextFileReaderService created");
        isRunning = true;
        myBinder = new Binder();
    }

    @Override
    public IBinder onBind(Intent intent){
        bindingIntent = intent;
        Bundle extras = bindingIntent.getExtras();
        if(extras != null)
            Log.d(TAG, "Binding to " + extras.getString("origin"));
        Log.d(TAG, "Returning binder");
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started by " + startId + " because of " + intent);
        return START_STICKY; //Keep going until explicitly stopped
    }

    @Override
    public boolean onUnbind(Intent unbindIntent){
        super.onUnbind(unbindIntent);
        lastSentenceReadBeforeUnbinding = lastSentenceRead;
        return true;
    }

    @Override
    public void onRebind(Intent rebindIntent) {
        super.onRebind(rebindIntent);
        Log.d(TAG, "Rebinding to " + rebindIntent.toString());
    }


    //TODO: Add a Thread to use to read the text file, and then simulate the gaps between each
    //message being received by the server by making that Thread sleep for that time.
    public ArrayList<Aircraft> readFromTextFile(Context context, ArrayList<Aircraft> aircraftArrayList) {
        int count = 0;
        Scanner s = new Scanner(context.getResources().openRawResource(R.raw.samplelog));
        try {
            while (s.hasNext()) {
                count++;
                lastSentenceRead = count;
                final String word = s.next(); //.trim(); //remove whitespaces from the line being read
                String[] splitLine = word.split(","); //split the line from the log using the comma
                //24 Mar: Cut out the Integer.toString() calls to cut down on processing overhead
                Log.d(TAG, "Line #" + count + " from sample log = " + word +
                        ", has " + splitLine.length + " elements.");
                //This prevents messages without the requisite amount of fields getting parsed and screwing things up.
                if (splitLine.length == 22) {
                    if (lastSentenceReadBeforeUnbinding <= count) {
                        Runnable msgTask = new Runnable() {
                            @Override
                            public void run() {
                                sendMessageToClients(word);
                            }
                        };
                        delaySimulator.postDelayed(msgTask, 150);
                    }
                    Aircraft newAircraft = sbsDecoder.parseSBSMessage(splitLine);
                    if(newAircraft.latitude != null && newAircraft.longitude != null)
                        newAircraft.path.add(newAircraft.getPosition());
                    if(splitLine[7].length() > 6)
                        delay = Double.parseDouble(splitLine[7].substring(6)) - delay;
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
            ArrayList<Aircraft> aircraftListToCompare = new ArrayList<>();
            for(Aircraft a : aircraftArrayList){
                //Latitude and longitude are defined at the same time, this is cutting down on
                //redundant checks
                if(a.altitude != null && a.latitude != null){
                    Log.d(TAG, "Adding aircraft " + a.icaoHexAddr);
                    aircraftListToCompare.add(a);
                }
            }
            for(Aircraft a : aircraftListToCompare){
                Aircraft nearestAircraft = new Aircraft();
                double lowest2DDist = 99999.9;
                double lowest3DDist = 99999.9;
                for(Aircraft b : aircraftListToCompare){
                    //Stops us from comparing the same Aircraft against itself
                    if(!a.equals(b)){
                        double twoDDist = distCalc.twoDDistanceBetweenAircraft(a, b);
                        double threeDDist = distCalc.threeDDistanceBetweenAircraft(a, b);
                        Log.d(TAG, "2D distance between " + a.icaoHexAddr + " and " + b.icaoHexAddr + "= " + Double.toString(twoDDist) + "km");
                        Log.d(TAG, "3D distance between " + a.icaoHexAddr + " and " + b.icaoHexAddr + "= " + Double.toString(threeDDist) + "km");
                        if(twoDDist < lowest2DDist && threeDDist < lowest3DDist) {
                            lowest2DDist = twoDDist;
                            lowest3DDist = threeDDist;
                            nearestAircraft = b;
                            a.nearestNeighbour = b;
                            b.nearestNeighbour = a;
                            a.twoDDistToNN = twoDDist;
                            b.twoDDistToNN = twoDDist;
                            a.threeDDistToNN = threeDDist;
                            b.threeDDistToNN = threeDDist;
                        }
                    }
                }
                Log.d(TAG, "The closest aircraft to " + a.icaoHexAddr + " is " + nearestAircraft.toString());
            }
        }
        return aircraftArrayList;
    }

    public static boolean isRunning() {
        Log.d("TextFileReaderService", "Is this running? " + Boolean.toString(isRunning));
        return isRunning;
    }

    /**
     * Sends messages to any client classes which are listening
     * @param message - the String to be sent to the client class(es)
     */
    private void sendMessageToClients(String message){
        Log.d(TAG, "Message to send: " + message + " to " +  messageClientList.size() + " clients.");
        for(Messenger messenger : messageClientList){
            try {
                Bundle bundle = new Bundle();
                bundle.putString("sbsSampleLog", message); //As distinguished from "sbsMessage" from the SocketService
                Message msg = Message.obtain(null, MSG_START_READING);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                messageClientList.remove(messenger);
            }
        }
    }

    public static ArrayList<Aircraft> getAircraftArrayList(){
        return aircraftArrayList;
    }

    public class TextFileBinder extends Binder {
        public TextFileReaderService getService(){
            return TextFileReaderService.this;
        }
    }

    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            Log.d(TAG, "Message = " + msg);
            switch (msg.what) {
                case MSG_START_READING:
                    Log.d(TAG, "Message = " + msg.arg1);
                    long readingStart = SystemClock.currentThreadTimeMillis();
                    if(!finishedReading) {
                        aircraftArrayList = readFromTextFile(getApplicationContext(), new ArrayList<Aircraft>());
                    }
                    Log.d(TAG, "Finished reading");
                    finishedReading = true;
                    break;
                case MSG_REG_CLIENT:
                    messageClientList.add(msg.replyTo);
                    break;
                case MSG_UNREG_CLIENT:
                    messageClientList.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    }
}

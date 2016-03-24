package com.example.se415017.maynoothskyradar.services;

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
import android.util.Log;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.helpers.DistanceCalculator;
import com.example.se415017.maynoothskyradar.helpers.SBSDecoder;
import com.example.se415017.maynoothskyradar.objects.Aircraft;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Scanner;

//TODO: Convert this to a Service, just like SocketService
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

    public static final int MESSAGE = 2; //SocketService's MESSAGE value is 1
    public static final int MSG_REG_CLIENT = 4;
    public static final int MSG_UNREG_CLIENT = 6;

    public static final String AIR_KEY = "aircraftKey";

    public static boolean isRunning = false;

    Intent bindingIntent;
    //Allows for communication with MainActivity
    IBinder myBinder = new TextFileBinder();
    Messenger messenger = new Messenger(new IncomingHandler());
    Messenger replyMessenger;
    ArrayList<Messenger> messageClientList = new ArrayList<>();

    //Basic constructor class
    public TextFileReaderService(){
        sbsDecoder = new SBSDecoder();
        distCalc = new DistanceCalculator();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        myBinder = new Binder();
    }

    @Override
    public IBinder onBind(Intent intent){
        bindingIntent = intent;
        Log.d(TAG, "Binding to MainActivity");
        readFromTextFile(getApplicationContext(), new ArrayList<Aircraft>());
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started by " + startId + " because of " + intent);
        return START_STICKY; //Keep going until explicitly stopped
    }

    //TODO: Add a Thread to use to read the text file, and then simulate the gaps between each
    //message being received by the server by making that Thread sleep for that time.
    public void readFromTextFile(Context context, ArrayList<Aircraft> aircraftArrayList) {
        int count = 0;
        Scanner s = new Scanner(context.getResources().openRawResource(R.raw.samplelog));
        try {
            while (s.hasNext()) {
                count++;
                String word = s.next(); //.trim(); //remove whitespaces from the line being read
                sendMessageToClients(word);
                String[] splitLine = word.split(","); //split the line from the log using the comma
                Log.d(TAG, "Line #" + Integer.toString(count) + " from sample log = " + word +
                        ", has " + Integer.toString(splitLine.length) + " elements.");
                //This prevents messages without the requisite amount of fields getting parsed and screwing things up.
                if(splitLine.length == 22) {
                    Aircraft newAircraft = sbsDecoder.parseSBSMessage(splitLine);
                    if(newAircraft.latitude != null && newAircraft.longitude != null){
                        newAircraft.path.add(newAircraft.getPosition());
                    }
                    if(splitLine[7].length() > 6){
                        delay = Double.parseDouble(splitLine[7].substring(6)) - delay;
                    }
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
        //return s.toString();
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
                Message msg = Message.obtain(null, MESSAGE);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                messageClientList.remove(messenger);
            }
        }
    }

    public class TextFileBinder extends Binder {
        public TextFileReaderService getService(){
            return TextFileReaderService.this;
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MESSAGE:
                    Log.d(TAG, "Message = " + msg.arg1);
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

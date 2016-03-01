package com.example.se415017.maynoothskyradar.helpers;

import android.util.Log;

import com.example.se415017.maynoothskyradar.objects.Aircraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InterfaceAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by se415017 on 09/11/2015.
 */
public class SBSDecoder {
    //TODO: Sockets sorted out, this is kind of useless
    private Socket socket;
    private int port = 9999; // default
    private String drBrownsServer = "192.168.1.1"; // default
    String TAG = getClass().getSimpleName();
    OutputStream out = null;
    InputStream in = null;
    byte[] buffer = new byte[128]; // adsMonitor.py uses 128-byte buffer
    /**
     * Just a basic constructor for the decoder
     */
    public SBSDecoder() {

    }
//    public SBSDecoder(String serverURL, int serverPort){
//        this.port = serverPort;
//        this.drBrownsServer = serverURL;
//        try {
//            socket = new Socket(drBrownsServer, port);
//            receive(socket);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public byte[] receive(Socket s) throws IOException {
        //Redundant
        in = s.getInputStream();
        int result = in.read();
        for (int i = 0; i < 128; i++){
            buffer[i] = Byte.parseByte(Integer.toBinaryString(result), 2);
        }
        Log.d(getClass().toString(), buffer.toString());
        return buffer;
    }

    /**
     * This method takes in the Mode-S code and converts it to the aircraft's callsign
     * @param modeSString
     * @return callsign - the callsign of the aircraft
     */
    public String modeSToCallsign(String modeSString) {
        String callsign = "";
        return callsign;
    }

    /**
     * Parses the SBS-1 message and uses that data to create an Aircraft object.
     * @param sbsMessageArray The array of strings created when splitting the SBS-1 messages
     * @return aircraftToAddOrModify The Aircraft object created by the aforementioned array of strings
     */
    public Aircraft parseSBSMessage(String[] sbsMessageArray) {
        Aircraft aircraftToAddOrModify = new Aircraft();
        //By checking for 22 fields, we don't get thrown off by transmission messages without that amount of fields
        //if(sbsMessageArray.length == 22) {
        //The above is redundant thanks to the array length checking done in readFromTextFile()
        //sbsMessageArray[1] is the type of transmission message
        aircraftToAddOrModify.icaoHexAddr = sbsMessageArray[4];
        switch (sbsMessageArray[0]) {
            case "MSG":
                switch (Integer.parseInt(sbsMessageArray[1])) {
                    case 1:
                        Log.d(TAG, "Callsign = " + sbsMessageArray[10]);
                        aircraftToAddOrModify.callsign = sbsMessageArray[10];
                        break;
                    case 2:
                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                        Log.d(TAG, "Ground speed = " + sbsMessageArray[12] + "kts");
                        Log.d(TAG, "Track = " + sbsMessageArray + "\u00b0");
                        Log.d(TAG, "Latitude = " + sbsMessageArray[14]);
                        Log.d(TAG, "Longitude = " + sbsMessageArray[15]);
                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
                        aircraftToAddOrModify.gSpeed = Integer.parseInt(sbsMessageArray[12]);
                        aircraftToAddOrModify.track = Integer.parseInt(sbsMessageArray[13]);
                        aircraftToAddOrModify.latitude = Double.parseDouble(sbsMessageArray[14]);
                        aircraftToAddOrModify.longitude = Double.parseDouble(sbsMessageArray[15]);
                        break;
                    case 3:
                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                        Log.d(TAG, "Latitude = " + sbsMessageArray[14]);
                        Log.d(TAG, "Longitude = " + sbsMessageArray[15]);
                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
                        aircraftToAddOrModify.latitude = Double.parseDouble(sbsMessageArray[14]);
                        aircraftToAddOrModify.longitude = Double.parseDouble(sbsMessageArray[15]);
                        break;
                    case 4:
                        Log.d(TAG, "Ground speed = " + sbsMessageArray[12] + "kts");
                        Log.d(TAG, "Track = " + sbsMessageArray[13] + "\u00b0");
                        Log.d(TAG, "Climbing at " + sbsMessageArray[16] + "ft/min");
                        aircraftToAddOrModify.gSpeed = Integer.parseInt(sbsMessageArray[12]);
                        aircraftToAddOrModify.track = Integer.parseInt(sbsMessageArray[13]);
                        break;
                    //"OR" operators in switch statements was a bad idea
                    case 5:
                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
                        break;
                    case 6:
                        Log.d(TAG, "Squawk = " + sbsMessageArray[17]);
                        break;
                    case 7:
                        Log.d(TAG, "Altitude = " + sbsMessageArray[11] + "ft");
                        aircraftToAddOrModify.altitude = Integer.parseInt(sbsMessageArray[11]);
                        break;
                    case 8:
                        //Log.d(TAG, "Is " + sbsMessageArray[4] + " on the ground? " + Boolean.toString(sbsMessageArray[21].equals("1")));
                        break;
                }
                break;
            default:
                break;
        }
        Log.d(TAG, "Aircraft status = " + sbsMessageArray[1] + ", " + aircraftToAddOrModify.toString() + " ******");
        return aircraftToAddOrModify;
    }

    /**
     * Searches through an ArrayList of Aircraft for a given Aircraft object, then returns a modified
     * form of that ArrayList with that Aircraft added or modified.
     * @param aircraftArrayList The ArrayList containing all Aircraft from which messages have been
     *                          received so far.
     * @param aircraftToSearchFor The Aircraft which we've received a transmission message from, and
     *                            which we're searching through the ArrayList for.
     * @param transMessageType The type of transmission message received from the aircraft we're
     *                         searching through the ArrayList for.
     * @return aircraftArrayList A modified form of the ArrayList entered into this method at first.
     */
    public ArrayList<Aircraft> searchThroughAircraftList(ArrayList<Aircraft> aircraftArrayList,
                                                         Aircraft aircraftToSearchFor,
                                                         int transMessageType){
        //Checks if an aircraft with a given ICAO hex code is found in the list
        boolean hexIdentFound = false;

        //There's no point iterating through an empty list.
        if (aircraftArrayList.size() > 0) {
            //foreach loops were causing ConcurrentModificationExceptions
            for (int i = 0; i < aircraftArrayList.size(); i++) {
                //This Aircraft object is just something to compare newly-discovered ones against
                Aircraft aircraftToCompare = aircraftArrayList.get(i);
                hexIdentFound = aircraftToCompare.icaoHexAddr.equals(aircraftToSearchFor.icaoHexAddr);
                if (hexIdentFound) {
                    Log.d(TAG, "Updating " + aircraftToSearchFor.icaoHexAddr + ", current status: "
                            + aircraftToCompare.toString());
                    /**
                     * Some fields aren't included in different transmission message types. So,
                     * if we receive subsequent messages from an aircraft, we fill in missing
                     * fields in messages using its last known values for each field.
                     *
                     * For example, MSG,1 just has the callsign of an aircraft. We get its
                     * altitude, ground speed, track, latitude & longitude from the
                     * corresponding Aircraft object.
                     */
                    switch (transMessageType){
                        case 1:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 1");
                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
                            aircraftToSearchFor.track = aircraftToCompare.track;
                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
                            break;
                        case 2:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 2");
                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
                            aircraftToSearchFor.track = aircraftToCompare.track;
                            break;
                        case 3:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 3");
                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
                            aircraftToSearchFor.track = aircraftToCompare.track;
                            break;
                        case 4:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 4");
                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
                            break;
                        case 5:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 5");
                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
                            aircraftToSearchFor.track = aircraftToCompare.track;
                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
                            break;
                        case 6:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 6");
                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
                            aircraftToSearchFor.track = aircraftToCompare.track;
                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
                            break;
                        case 7:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 7");
                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
                            aircraftToSearchFor.track = aircraftToCompare.track;
                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
                            break;
                        case 8:
                            Log.d(TAG, aircraftToSearchFor.icaoHexAddr + " Modified Aircraft, case 8");
                            aircraftToSearchFor.callsign = aircraftToCompare.callsign;
                            aircraftToSearchFor.altitude = aircraftToCompare.altitude;
                            aircraftToSearchFor.gSpeed = aircraftToCompare.gSpeed;
                            aircraftToSearchFor.track = aircraftToCompare.track;
                            aircraftToSearchFor.latitude = aircraftToCompare.latitude;
                            aircraftToSearchFor.longitude = aircraftToCompare.longitude;
                            break;
                    }
                    //TODO: New aircraft are being added
                    Log.d(TAG, "Modified aircraft status: " + aircraftToSearchFor.toString());
                    aircraftArrayList.set(i, aircraftToSearchFor); //Add the modified Aircraft object to the ArrayList
                    break; //No need to keep checking the list
                }
            }
            //We've iterated through the entire Aircraft list and haven't found aircraftToSearchFor,
            //so we'll add it to the list.
            if (!hexIdentFound) {
                Log.d(TAG, "Adding new aircraft to list: " + aircraftToSearchFor.toString());
                aircraftArrayList.add(aircraftToSearchFor);
            }
        } else {
            //No aircraft in aircraftArrayList, now adding the first one to be discovered
            Log.d(TAG, "No aircraft found in list, now adding a new aircraft.");
            aircraftArrayList.add(aircraftToSearchFor);
        }
        Log.d(TAG, "AircraftArrayList now contains " + aircraftArrayList.size() + " aircraft.");
        return aircraftArrayList;
    }
}

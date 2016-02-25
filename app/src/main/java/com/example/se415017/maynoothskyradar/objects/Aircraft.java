package com.example.se415017.maynoothskyradar.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by se415017 on 10/11/2015.
 *
 *
 */
public class Aircraft extends RealmObject {
    @PrimaryKey
    private String icaoHexAddr;
    //String flightNum; //Not much point in this, an aircraft would do multiple flight routes - now redundant thanks to callsign
    //private String regCode = "TBD"; // registration code of the plane, I'll probably fetch this from a database
    private String callsign = "TBD";
    private int altitude; // Mode-C altitude (i.e. relative to 1.0132 bar), not it's actual distance above mean sea level
    private int gSpeed; //Ground speed, not indicated airspeed
    private int track; //Distinct from its heading, derived from its E/W and N/S velocities.
    private double latitude, longitude;

    //Default public constructor with no argument
    public Aircraft(){
        new Aircraft("", "TBD", 0, 0, 0, 0.0, 0.0);
    }
    /**
     * This is a basic constructor of the aircraft, it's for when it's been newly identified but
     * its callsign hasn't shown up in a message yet.
     *
     * @param icaoHexAddr The aircraft's Mode-S hexadecimal identification code
     * @param callsign The flight number of the aircraft
     * @param altitude The altitude of the aircraft, measured in feet
     * @param gSpeed The ground speed of the aircraft, measured in knots
     * @param track The actual direction of the aircraft, not its heading
     * @param latitude The latitude of the aircraft
     * @param longitude The longitude of the aircraft
     */
    public Aircraft(String icaoHexAddr, String callsign, int altitude, int gSpeed, int track, double latitude, double longitude){
        this.icaoHexAddr = icaoHexAddr;
        this.callsign = callsign;
        this.altitude = altitude;
        this.gSpeed = gSpeed;
        this.track = track;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIcaoHexAddr()  { return icaoHexAddr; }
    public void setIcaoHexAddr(String icaoHexAddr) { this.icaoHexAddr = icaoHexAddr; }

    public String getCallsign()     { return callsign; }
    public void setCallsign(String callsign)    { this.callsign = callsign; }

    public int getAltitude()        { return altitude; }
    public void setAltitude(int altitude)       { this.altitude = altitude; }

    public int getgSpeed()          { return gSpeed; }
    public void setgSpeed(int gSpeed)           { this.gSpeed = gSpeed; }

    public int getTrack()           { return track; }
    public void setTrack(int track)             { this.track = track; }

    public double getLatitude()     { return latitude; }
    public void setLatitude(double latitude)    { this.latitude = latitude; }

    public double getLongitude()    { return longitude; }
    public void setLongitude(double longitude)  { this.longitude = longitude; }
}
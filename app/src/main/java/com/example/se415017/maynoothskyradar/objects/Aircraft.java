package com.example.se415017.maynoothskyradar.objects;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by se415017 on 10/11/2015.
 * 2 March 2016
 *
 */
public class Aircraft {
    @PrimaryKey
    public String icaoHexAddr = " ";
    //private String regCode = "TBD"; // registration code of the plane, I'll probably fetch this from a database
    public String callsign = " "; // replaces FlightNum
    public String altitude; // Mode-C altitude (i.e. relative to 1.0132 bar), not it's actual distance above mean sea level
    public String gSpeed; //Ground speed, not indicated airspeed
    public String track; //Distinct from its heading, derived from its E/W and N/S velocities.
    public String latitude, longitude;
    public int climbRate; //

    //Default public constructor with no argument
    public Aircraft(){
        new Aircraft(" ");
    }

    //Public constructor, used when detecting a new aircraft.
    //The parameters besides icaoHexAddr are left blank, to be updated later upon receiving a new
    //SBS-1 message from that aircraft.
    public Aircraft(String icaoHexAddr) {
        new Aircraft(icaoHexAddr, "", "", "", "", "", "");
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
    public Aircraft(String icaoHexAddr, String callsign, String altitude, String gSpeed,
                    String track, String latitude, String longitude){
        this.icaoHexAddr = icaoHexAddr;
        this.callsign = callsign;
        this.altitude = altitude;
        this.gSpeed = gSpeed;
        this.track = track;
        this.latitude = latitude;
        this.longitude = longitude;
    }

//  Redundant as of 2 March as I can just access attributes of Aircraft objects like "Aircraft.latitude"
//  rather than "Aircraft.getLatitude()". Besides, it's more resource-intensive to use getters and setters.
//   public String getIcaoHexAddr()  { return icaoHexAddr; }
//    public void setIcaoHexAddr(String icaoHexAddr) { this.icaoHexAddr = icaoHexAddr; }
//
//    public String getCallsign()     { return callsign; }
//    public void setCallsign(String callsign)    { this.callsign = callsign; }
//
//    public int getAltitude()        { return altitude; }
//    public void setAltitude(int altitude)       { this.altitude = altitude; }
//
//    public int getgSpeed()          { return gSpeed; }
//    public void setgSpeed(int gSpeed)           { this.gSpeed = gSpeed; }
//
//    public int getTrack()           { return track; }
//    public void setTrack(int track)             { this.track = track; }
//
//    public double getLatitude()     { return latitude; }
//    public void setLatitude(double latitude)    { this.latitude = latitude; }
//
//    public double getLongitude()    { return longitude; }
//    public void setLongitude(double longitude)  { this.longitude = longitude; }

    //Returns all of the attributes of the aircraft in a single string
    public String toString() {
        return icaoHexAddr + ", " + callsign + ", " + altitude + "ft, " +
                gSpeed + "kts, " + track + "\u00b0, " +
                latitude + ", " + longitude;
    }

    //Returns the coordinates of the aircraft in a LatLng object
    public LatLng getPosition() {
        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    //Returns the coordinates of the aircraft as a string
    public String getPosString() {
        return latitude + ", " + longitude;
    }
}

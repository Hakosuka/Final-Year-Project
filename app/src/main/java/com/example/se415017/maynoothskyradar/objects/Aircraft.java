package com.example.se415017.maynoothskyradar.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by se415017 on 10/11/2015.
 */
public class Aircraft extends RealmObject {
    @PrimaryKey
    private String icaoHexAddr;
    //String flightNum; //Not much point in this, an aircraft would do multiple flight routes
    private String regCode = "TBD"; // registration code of the plane, I'll probably fetch this from a database
    private String callsign = "TBD";
    private int altitude;
    /*private int speed;
    private int track;*/
    private double latitude, longitude;

    /**
     *
     * @param icaoHexAddr
     * @param altitude
     * @param latitude
     * @param longitude
     */
    public Aircraft(String icaoHexAddr, int altitude, double latitude, double longitude){
        new Aircraft(icaoHexAddr, "TBD", altitude, latitude, longitude);
    }

    public Aircraft(String icaoHexAddr, String callsign, int altitude, double latitude, double longitude){
        this.icaoHexAddr = icaoHexAddr;
        this.callsign = callsign;
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIcaoHexAddr()  { return icaoHexAddr; }
    public void setIcaoHexAddr(String icaoHexAddr) { this.icaoHexAddr = icaoHexAddr; }

    public String getRegCode()      { return regCode; }
    public void setRegCode(String regCode) { this.regCode = regCode; }

    public String getCallsign()     { return callsign; }
    public void setCallsign(String callsign)    { this.callsign = callsign; }

    public int getAltitude()        { return altitude; }
    public void setAltitude(int altitude)       { this.altitude = altitude; }

    public double getLatitude()     { return latitude; }
    public void setLatitude(double latitude)    { this.latitude = latitude; }

    public double getLongitude()    { return longitude; }
    public void setLongitude(double longitude)  { this.longitude = longitude; }

    public String getAircraftStatus() {
        return icaoHexAddr + ", " + callsign + ", " + Integer.toString(altitude) + ", " +
                Double.toString(latitude) + ", " + Double.toString(longitude);
    }
}

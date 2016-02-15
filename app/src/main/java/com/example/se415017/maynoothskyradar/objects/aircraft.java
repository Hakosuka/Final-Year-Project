package com.example.se415017.maynoothskyradar.objects;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by se415017 on 10/11/2015.
 */
public class Aircraft extends RealmObject {
    @PrimaryKey private String icaoHexAddr;
    //String flightNum; //Not much point in this, an aircraft would do multiple flight routes
    private String regCode = "TBD"; // registration code of the plane, I'll probably fetch this from a database
    private int altitude;
    /*private int speed;
    private int track;*/
    private double lat, lon;

    public String getIcaoHexAddr() { return icaoHexAddr; }
    public void setIcaoHexAddr(String icaoHexAddr) { this.icaoHexAddr = icaoHexAddr; }

    public String getRegCode() { return regCode; }
    public void setRegCode(String regCode) { this.regCode = regCode; }

    public double[] getLocation(){
        double[] latAndLon = {lat, lon};
        return latAndLon;
    }
    public void setLocation(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public int getAltitude() { return altitude; }
    public void setAltitude(int altitude){
        this.altitude = altitude;
    }
}

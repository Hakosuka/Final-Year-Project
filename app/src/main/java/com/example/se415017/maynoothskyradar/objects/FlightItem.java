package com.example.se415017.maynoothskyradar.objects;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by se415017 on 10/11/2015.
 */
public class FlightItem extends RealmObject {
    @PrimaryKey
    private String icaoHexAddr;
    //String flightNum; //Not much point in this, an aircraft would do multiple flight routes
    private String regCode = "TBD"; // registration code of the plane, I'll probably fetch this from a database
    private int altitude;
    /*private int speed;
    private int track;*/
    private double latitude, longitude;

    public String getIcaoHexAddr()  { return icaoHexAddr; }
    public void setIcaoHexAddr(String icaoHexAddr) { this.icaoHexAddr = icaoHexAddr; }

    public String getRegCode()      { return regCode; }
    public void setRegCode(String regCode) { this.regCode = regCode; }

    public double getLatitude()     { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude()    { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getAltitude()        { return altitude; }
    public void setAltitude(int altitude){
        this.altitude = altitude;
    }
}

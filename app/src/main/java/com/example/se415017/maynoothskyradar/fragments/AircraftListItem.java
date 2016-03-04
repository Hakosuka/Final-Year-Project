package com.example.se415017.maynoothskyradar.fragments;

import com.example.se415017.maynoothskyradar.objects.Aircraft;

/**
 * Created by se415017 on 04/03/2016.
 *
 * This is meant to be an item representing an Aircraft object in the AircraftListFragment.
 */
public class AircraftListItem {
    private Aircraft aircraft;

    private String hexContent;
    private String callsignContent;
    private String altitudeContent;
    private String latitudeContent;
    private String longitudeContent;

    //Constructor method
    public AircraftListItem(Aircraft aircraft){
        this.aircraft = aircraft;
    }

    public String getHexContent() {
        return hexContent;
    }

    public String getCallsignContent() {
        return callsignContent;
    }

    public String getAltitudeContent() {
        return altitudeContent;
    }

    public String getLatitudeContent() {
        return latitudeContent;
    }

    public String getLongitudeContent() {
        return longitudeContent;
    }

    public void setHexContent(String hexContent) {
        this.hexContent = hexContent;
    }

    public void setCallsignContent(String callsignContent) {
        this.callsignContent = callsignContent;
    }

    public void setAltitudeContent(String altitudeContent) {
        this.altitudeContent = altitudeContent;
    }

    public void setLatitudeContent(String latitudeContent) {
        this.latitudeContent = latitudeContent;
    }

    public void setLongitudeContent(String longitudeContent) {
        this.longitudeContent = longitudeContent;
    }
}

package com.example.se415017.maynoothskyradar.presenters;

import android.os.Bundle;
import android.util.Log;

import com.example.se415017.maynoothskyradar.fragments.GPSTestFragment;

import nucleus.presenter.RxPresenter;
import nucleus.view.NucleusFragment;

/**
 * Created by se415017 on 24/11/2015.
 */
public class GPSTestPresenter extends RxPresenter<GPSTestFragment> {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }
    public String[] decodeNMEA(String sentence){
        String tag = "Decoding NMEA";
        if(sentence.startsWith("$GPRMC")) {
            String[] rmcValues = sentence.split(",");
            double nmeaLatitude = Double.parseDouble(rmcValues[3]);
            double nmeaLatMin = nmeaLatitude % 100; //get minutes from latitude value
            nmeaLatitude/=100;
            /*if(rmcValues[4].charAt(0)=='S'){
                nmeaLatitude = -nmeaLatitude;
            } commented out, I'll just concatenate this string onto nmeaLatitude's string form */
            double nmeaLongitude = Double.parseDouble(rmcValues[5]);
            double nmeaLonMin = nmeaLongitude % 100; //get minutes from longitude value
            nmeaLongitude/=100;
            /*if(rmcValues[6].charAt(0)=='W'){
                nmeaLongitude = -nmeaLongitude;
            } commented out, I'll just concatenate this string onto nmeaLongitude's string form */

            Log.d(tag + ": lat", Double.toString(nmeaLatitude));
            Log.d(tag + ": lon", Double.toString(nmeaLongitude));
            String[] returnedDecodeValues = {"Success", Double.toString(nmeaLatitude) + "&#176;" + rmcValues[4], Double.toString(nmeaLongitude) + "&#176;" + rmcValues[6]};
            return returnedDecodeValues;
        }
        else{
            String[] GPRMCfailure = {"Failure"};
            return GPRMCfailure;
        }
    }
}

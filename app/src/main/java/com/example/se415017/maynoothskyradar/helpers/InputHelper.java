package com.example.se415017.maynoothskyradar.helpers;

import android.util.Log;
import android.util.Patterns;

/**
 * Created by se415017 on 17/02/2016.
 *
 * Chances are that I'll need the "checkForValidLat()/Lon()/URLOrIP()" methods again in the
 * Settings menu of my app, so I might as well take them out of EnterURLFragment and into
 * here.
 */
public class InputHelper {

    //Constructor of the InputHelper class
    public InputHelper() {
        Log.d(getClass().getSimpleName(), "InputHelper created");
    }

    /**
     * Checks that the string entered by the user is a valid latitude value.
     * @param lat the string to be checked to ensure that it's a number between -90 and 90 (inclusive)
     * @return whether or not the entered number is between -90 and 90
     */
    public boolean checkForValidLat(String lat){
        return lat.matches("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)");
    }

    /**
     * Checks that the string entered by the user is a valid longitude value.
     * @param lon the string to be entered to ensure that it's a number between -180 and 180 (inclusive)
     * @return whether or not the entered number is between -180 and 180
     */
    public boolean checkForValidLon(String lon){
        return lon.matches("[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)");
    }

    /**
     * Checks that the user has entered a valid URL or IP address.
     * @param urlStr The URL (or IP address) of the server entered by the user
     * @return whether or not the entered string represents a valid URL/IP address
     */
    public boolean checkForValidURLOrIP(String urlStr){
        return (Patterns.WEB_URL.matcher(urlStr).matches()
                || Patterns.IP_ADDRESS.matcher(urlStr).matches());
    }
}

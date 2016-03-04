package com.example.se415017.maynoothskyradar.helpers;

import com.example.se415017.maynoothskyradar.objects.Aircraft;

/**
 * Created by se415017 on 02/03/2016.
 *
 * This calculates the distance between two aircraft in 2D or 3D.
 */
public class DistanceCalculator {
    double earthRadius = 6371.0; //Earth's mean radius in kilometres

    //Basic constructor for this class
    public DistanceCalculator() {

    }

    /**
     * This method uses the haversine formula to calculate the distance (excluding altitude)
     * between two aircraft
     * @param aircraft1 The first aircraft involved in this
     * @param aircraft2
     * @return The distance between the two aircraft in kilometres
     */
    public double twoDDistanceBetweenAircraft(Aircraft aircraft1, Aircraft aircraft2){
        double lat1 = Double.parseDouble(aircraft1.latitude);
        double lat2 = Double.parseDouble(aircraft2.latitude);
        double lon1 = Double.parseDouble(aircraft1.longitude);
        double lon2 = Double.parseDouble(aircraft2.longitude);
        double dLatRads = Math.toRadians(lat2 - lat1);
        double dLonRads = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLatRads/2) * Math.sin(dLatRads/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLonRads/2) * Math.sin(dLonRads/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return earthRadius * c;
    }

    public double threeDDistanceBetweenAircraft(Aircraft aircraft1, Aircraft aircraft2){
        double twoDDistance = twoDDistanceBetweenAircraft(aircraft1, aircraft2);
        /**
         * Get the square of twoDDistance and add it to the square of the difference in altitude
         * between the two aircraft, and THEN get the square root of the result. That should be the
         * distance between the two aircraft in 3D space.
         *
         * Also, as this calculates the distance between aircraft in metres, and the Aircraft
         * object's altitude is in feet, the difference in altitude between two of them is
         * multiplied by 0.3048 (as 0.3048m = 1ft), and THEN multiplied by 0.001 to give the
         * result in kilometres.
         */
        return Math.sqrt(Math.pow(twoDDistance, 2) +
                Math.pow((Double.parseDouble(aircraft1.altitude) -
                                Double.parseDouble(aircraft2.altitude))*0.3048*0.001, 2));
    }
}

package com.example.rachel.wygt;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Rachel on 10/22/14.
 */
public class LatLong implements Serializable{

    private double latitude, longitude;

    public LatLong(LatLng location){
        latitude = location.latitude;
        longitude = location.longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String toString(){
        return "lat: "+latitude+" long: "+longitude;
    }
}

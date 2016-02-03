package com.magostinhojr.currentlocation.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by marceloagostinho.
 */
public abstract class AbstractLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
        Log.d("CurrentLocation", "onLocationChanged");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("CurrentLocation", "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("CurrentLocation", "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("CurrentLocation", "onProviderDisabled");
    }
}

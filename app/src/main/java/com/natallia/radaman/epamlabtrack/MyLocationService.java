package com.natallia.radaman.epamlabtrack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A service that provides a data about changes in the location of the phone (longitude and
 * latitude) and a timestamp.
 * <p>To see the service life cycle, you need to uncomment part of the code (Log.i(), Log.d().</p>
 */
public class MyLocationService extends Service implements Runnable {
    private static boolean isServiceRunning;
    private static final String TAG = "MyLocationService";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private Handler handler;
    private LocationManager mLocationManager = null;
    private Location mLastLocation;
    private boolean isNotified;
    private List<RouteLocation> routeList;

    private Location oldLocation;
    private Location newLocation;

    private LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.PASSIVE_PROVIDER),
            new LocationListener(LocationManager.GPS_PROVIDER)
    };

    @Override
    public void onCreate() {

        //Log.e(TAG, "onCreate");

        handler = new Handler();
        this.isNotified = false;
        this.run();

        isServiceRunning = true;

        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "Request location update fails, ignore it", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "Network provider doesn\'t exist, " + ex.getMessage());
        }
    }

    private void initializeLocationManager() {
//        Log.e(TAG, "initialize LocationManager. LOCATION_INTERVAL: " + LOCATION_INTERVAL
//                + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    public void stopRepeatingTask() {
        handler.removeCallbacks(this);
    }

    public String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find today date
            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
//            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
//            Log.e(TAG, "onLocationChanged: " + location);
            isNotified = false;
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
//            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
//            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        Log.e(TAG, "onDestroy");
        isServiceRunning = false;
        stopRepeatingTask();
        super.onDestroy();

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                            .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                            .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "Couldn\'t remove location listener, ignore it", ex);
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        try {
            if (getLastLocation() != null && !isNotified) {

                this.isNotified = true;

                System.out.println(getLastLocation());
                String distanceBetween = "0.0";

                if (newLocation != null)
                    this.oldLocation = new Location(this.newLocation);

                this.newLocation = new Location(getLastLocation());

                if (oldLocation != null) {
                    distanceBetween = String.valueOf(oldLocation.distanceTo(newLocation));

                    String lat = String.valueOf(getLastLocation().getLatitude());
                    String lan = String.valueOf(getLastLocation().getLongitude());

                    routeList = RouteList.getWorkInstance().getPlaces();
                    routeList.add(new RouteLocation(getCurrentTimeStamp(), lat, lan, distanceBetween));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            handler.postDelayed(this, 2 * LOCATION_INTERVAL);
        }
    }
}

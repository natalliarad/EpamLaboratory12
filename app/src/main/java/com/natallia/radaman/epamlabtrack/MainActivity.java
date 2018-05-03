
package com.natallia.radaman.epamlabtrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Used to draw the user interface, Google map and handle the interaction with the Location
 * service.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    private GoogleMap mGoogleMap;
    private Location mLastLocation;
    private Polyline polylineRoute;
    private Marker markerCurrent;

    private Button btnStart, btnClear, btnEnd, btnReload;
    private List<RouteLocation> routeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.app_label);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            checkLocationPermission();
        else initGoogleMapLocation();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initView();
    }

    private void initView() {
        btnStart = (Button) findViewById(R.id.btnStart);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnEnd = (Button) findViewById(R.id.btnEnd);

        btnStart.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnEnd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        routeList = RouteList.getWorkInstance().getPlaces();
        switch (v.getId()) {
            case R.id.btnStart:
                routeList.clear();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(),
                                        MyLocationService.class);
                                startService(intent);
                            }
                        }, 100);
                    }
                }, 100);
                mGoogleMap.clear();
                break;
            case R.id.btnClear:
                routeList.clear();
                mGoogleMap.clear();
                break;
            case R.id.btnEnd:
                stopService(new Intent(getApplicationContext(), MyLocationService.class));
                drawPath();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            PermissionSettings.requestPermission(this);
        else
            initGoogleMapLocation();
    }

    private void initGoogleMapLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLocations().get(0);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(mLastLocation.getLatitude(), mLastLocation
                        .getLongitude()));
                BitmapDescriptor icon = BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
                markerOptions.icon(icon);
                markerCurrent = mGoogleMap.addMarker(markerOptions);
                mGoogleMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(markerCurrent.getPosition(), 17));
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);

            }
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings
                (mLocationSettingsRequest);
        locationResponse.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e("Response", "Successful acquisition of location information!!");
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                        Looper.myLooper());
                mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        LinearLayout info = new LinearLayout(getApplicationContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getApplicationContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getApplicationContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });
            }
        });
        locationResponse.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e("onFailure", "What");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Error";
                        Log.e("onFailure", errorMessage);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionSettings.REQUEST_CODE:
                //If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted. Do the location-related task you need to do.
                    initGoogleMapLocation();
                } else {
                    // permission denied. Disable the functionality that depends on this
                    // permission
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG)
                            .show();
                }
                return;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private void drawPath() {
        List<LatLng> latlngs = new ArrayList<>();
        for (RouteLocation route : routeList) {
            latlngs.add(new LatLng(Float.parseFloat(route.getLat()), Float.parseFloat(route
                    .getLan())));
        }
        PolylineOptions polylineOptions = new PolylineOptions().addAll(latlngs);
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(15);
        MarkerOptions markerOptionsStart = new MarkerOptions();
        if (latlngs.size() > 0) {
            markerOptionsStart.position(latlngs.get(0));
            BitmapDescriptor icon = BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
            markerOptionsStart.icon(icon);
            markerOptionsStart.title("Start of my route");
            markerOptionsStart.snippet(RouteList.timeOfTheStartPath(routeList));
            mGoogleMap.addMarker(markerOptionsStart);

            MarkerOptions markerOptionsEnd = new MarkerOptions();
            markerOptionsEnd.position(latlngs.get(latlngs.size() - 1));
            markerOptionsEnd.icon(icon);

            markerOptionsEnd.title("Stop of my route");
            markerOptionsEnd.snippet(RouteList.distanceOfPath(routeList) + "\n"
                    + RouteList.timeOfTheEndPath(routeList) + "\n"
                    + "You was walking about " +
                    RouteList.getDateDiff(routeList.get(0).getTime(),
                            routeList.get(routeList.size() - 1).getTime(), TimeUnit.MINUTES) + " minutes.");
            mGoogleMap.addMarker(markerOptionsEnd);
        }
        mGoogleMap.addPolyline(polylineOptions);
    }
}

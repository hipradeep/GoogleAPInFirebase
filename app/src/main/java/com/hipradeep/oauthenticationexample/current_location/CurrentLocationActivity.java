package com.hipradeep.oauthenticationexample.current_location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.hipradeep.oauthenticationexample.R;

public class CurrentLocationActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    TextView tv_current_location;
    Button btn_current_location;
    private static final int PERMISSION_REQUEST_CODE = 200;
    ResultReceiver resultReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        tv_current_location=findViewById(R.id.tv_current_location);
        btn_current_location=findViewById(R.id.btn_current_location);
        resultReceiver = new LocationAddressResultReceiver(new Handler());

        btn_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkLocationPermissions()){

                    if (isGPSProviderEnable()) {
                        getCurrentLocation();
                        //Toast.makeText(CurrentLocationActivity.this, "gps Enable", Toast.LENGTH_SHORT).show();
                    }else {
                        buildAlertMessageNoGps();
                    }
                }else {
                    requestLocationPermission();
                }

            }
        });
    }
    private boolean isGPSProviderEnable() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(CurrentLocationActivity.this);
        builder.setMessage("Your Location seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                        Toast.makeText(CurrentLocationActivity.this, "gps disable", Toast.LENGTH_SHORT).show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(CurrentLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CurrentLocationActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(CurrentLocationActivity.this, "Location Permission is Disable!", Toast.LENGTH_SHORT).show();
        } else {
            LocationServices.getFusedLocationProviderClient(CurrentLocationActivity.this)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(CurrentLocationActivity.this)
                                    .removeLocationUpdates(this);
                            if (locationResult.getLocations().size() > 0) {

                                int latestLocationIndex = locationResult.getLocations().size() - 1;
                              double  currentLatitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                              double  currentLongitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                              //  Toast.makeText(CurrentLocationActivity.this, "Latitude - "+currentLatitude+" Longitude -" +currentLongitude, Toast.LENGTH_SHORT).show();

                                tv_current_location.setText("Latitude - "+currentLatitude+"\nLongitude -" +currentLongitude);
                                Location location = new Location("providerNA");
                                location.setLatitude(currentLatitude);
                                location.setLongitude(currentLongitude);
                                getAddress(location);

                            }
                        }
                    }, Looper.getMainLooper());
        }

    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_REQUEST_CODE && grantResults[0]==PackageManager.PERMISSION_GRANTED){

            Toast.makeText(this, "Location Permission Granted!", Toast.LENGTH_SHORT).show();
            if(isGPSProviderEnable()){
                getCurrentLocation();
            }else {
                buildAlertMessageNoGps();
            }

        }else {
            Toast.makeText(CurrentLocationActivity.this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
            //showSettingsDialog();
        }
    }
    private void getAddress(Location location) {

        Intent intent = new Intent(CurrentLocationActivity.this, CurrentLocation.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    class LocationAddressResultReceiver extends ResultReceiver {

        LocationAddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
              String  currentLocation = resultData.getString(Constants.RESULT_DATA_KEY);
              Log.e(TAG, currentLocation);
              tv_current_location.append("\n"+currentLocation);
             //   Toast.makeText(CurrentLocationActivity.this, currentLocation, Toast.LENGTH_SHORT).show();
            } else if (resultCode == Constants.FAILURE_RESULT) {
                Toast.makeText(CurrentLocationActivity.this, "Address not found, ", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
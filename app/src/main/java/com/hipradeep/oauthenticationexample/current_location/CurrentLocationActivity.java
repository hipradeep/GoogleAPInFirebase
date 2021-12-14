package com.hipradeep.oauthenticationexample.current_location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.hipradeep.oauthenticationexample.R;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CurrentLocationActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "TAG";
    TextView tv_current_location;
    EditText et_autocomplete_places_api;
    Button btn_current_location, btn_goto_map;

    ResultReceiver resultReceiver;
    double currentLatitude = 0.0, currentLongitude = 0.0;
    boolean isLocationCoordinatesfind = false;
    boolean isNavigationChecked = false;
    CheckBox cb_navigation;
    List<Place.Field> fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        tv_current_location = findViewById(R.id.tv_current_location);
        btn_current_location = findViewById(R.id.btn_current_location);
        btn_goto_map = findViewById(R.id.btn_goto_map);
        cb_navigation = findViewById(R.id.cb_navigation);
        et_autocomplete_places_api = findViewById(R.id.et_autocomplete_places_api);
        resultReceiver = new LocationAddressResultReceiver(new Handler());

        btn_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkLocationPermissions()) {

                    if (isGPSProviderEnable()) {
                        getCurrentLocation();
                        //Toast.makeText(CurrentLocationActivity.this, "gps Enable", Toast.LENGTH_SHORT).show();
                    } else {
                        buildAlertMessageNoGps();
                    }
                } else {
                    requestLocationPermission();
                }

            }
        });

        et_autocomplete_places_api.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoLocationSearch();
            }
        });
        cb_navigation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.e("TAG", "B");
                    isNavigationChecked = true;
                } else {
                    Log.e("TAG", "A");
                    isNavigationChecked = false;
                }
            }
        });

        btn_goto_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (isLocationCoordinatesfind) {

                    if (isNavigationChecked) {
                        Uri navigation = Uri.parse("google.navigation:q=" + currentLatitude + "," + currentLongitude + "");
                        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigation);
                        navigationIntent.setPackage("com.google.android.apps.maps");
                        startActivity(navigationIntent);
                    } else {
                        String strUri = "http://maps.google.com/maps?q=loc:" + currentLatitude + "," + currentLongitude + " (" + "Your Current Location" + ")";
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(CurrentLocationActivity.this, "No Coordinates finds!", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void geoLocationSearch() {
        resultReceiver = new LocationAddressResultReceiver(new Handler());
        fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_place_api_key), Locale.US);
        }

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(CurrentLocationActivity.this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng qLoc = place.getLatLng();
                currentLatitude=qLoc.latitude;
                currentLongitude=qLoc.longitude;
                tv_current_location.setText("Latitude : " + currentLatitude + "\n\nLongitude :" + currentLongitude);
                Location location = new Location("providerNA");
                location.setLatitude(qLoc.latitude);
                location.setLongitude(qLoc.longitude);
                getAddress(location);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.d("DATAAAAAA", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

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
                                isLocationCoordinatesfind = true;
                                int latestLocationIndex = locationResult.getLocations().size() - 1;
                                currentLatitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                currentLongitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                                //  Toast.makeText(CurrentLocationActivity.this, "Latitude - "+currentLatitude+" Longitude -" +currentLongitude, Toast.LENGTH_SHORT).show();

                                tv_current_location.setText("Latitude - " + currentLatitude + "\n\nLongitude -" + currentLongitude);
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
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Location Permission Granted!", Toast.LENGTH_SHORT).show();
            if (isGPSProviderEnable()) {
                getCurrentLocation();
            } else {
                buildAlertMessageNoGps();
            }

        } else {
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
                String address = resultData.getString(Constants.RESULT_DATA_KEY);
                et_autocomplete_places_api.setText(address);
                tv_current_location.append("\n\n" + "Address : " + address);
            } else if (resultCode == Constants.CITY_SUCCESS_RESULT) {
                tv_current_location.append("\n\n" + "City : " + resultData.getString(Constants.RESULT_DATA_KEY));
            } else if (resultCode == Constants.STATE_SUCCESS_RESULT) {
                tv_current_location.append("\n\n" + "State : " + resultData.getString(Constants.RESULT_DATA_KEY));
            } else if (resultCode == Constants.POSTCODE_SUCCESS_RESULT) {
                tv_current_location.append("\n\n" + "pinCode : " + resultData.getString(Constants.RESULT_DATA_KEY));
            } else if (resultCode == Constants.FAILURE_RESULT) {
                Toast.makeText(CurrentLocationActivity.this, "Address not found, ", Toast.LENGTH_SHORT).show();
                // Log.d("Location", "Not");
            }
        }
    }
}
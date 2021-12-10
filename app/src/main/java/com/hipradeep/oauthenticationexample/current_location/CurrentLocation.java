package com.hipradeep.oauthenticationexample.current_location;


import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CurrentLocation extends IntentService {
    private static final String IDENTIFIER = "GetAddressIntentService";
    private ResultReceiver addressResultReceiver;

    public CurrentLocation() {
        super(IDENTIFIER);
    }


        /*
         * Add CurrentLocation Service in manifests

             <!--Service for getting current location-->
             <service android:name=".current_location.CurrentLocation" />

         *
         */

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String msg = "";
            addressResultReceiver = Objects.requireNonNull(intent).getParcelableExtra(Constants.RECEIVER);
            Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
            if (location == null) {
                msg = "No location, can't go further without location";
                sendResultsToReceiver(Constants.FAILURE_RESULT, msg);
                return;
            }
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (Exception ioException) {
                Log.e("", "Error in getting address for the location");
            }

            if (addresses == null || addresses.size() == 0) {
                msg = "No address found for the location";
                sendResultsToReceiver(Constants.FAILURE_RESULT, msg);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<>();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }

                String addressLine =  address.getAddressLine(0);
                String postCode =  address.getPostalCode() ;
                String city = address.getLocality();  /////get city
                String state = address.getAdminArea();  ///get state

                sendResultsToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(Objects.requireNonNull(System.getProperty("line.separator")), addressFragments));
                sendResultsToReceiver(Constants.POSTCODE_SUCCESS_RESULT, postCode);
                sendResultsToReceiver(Constants.CITY_STATE_SUCCESS_RESULT, city+", "+state);
                sendResultsToReceiver(Constants.ADDRESS_LINE_SUCCESS_RESULT, addressLine);
            }
        }
        if (addressResultReceiver == null) {
            Log.e("GetAddressIntentService", "No receiver, not processing the request further");
        }

    }

    private void sendResultsToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        addressResultReceiver.send(resultCode, bundle);
    }
}
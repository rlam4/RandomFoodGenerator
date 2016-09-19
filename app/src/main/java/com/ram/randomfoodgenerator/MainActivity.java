package com.ram.randomfoodgenerator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.yelp.clientlib.entities.Business;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "random-food-generator";
    public static final long UPDATE_INTERVAL = 1000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    protected static int PERMISSION_REQUEST_FINE_LOCATION = 1;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    protected Boolean mRequestingLocationUpdates = false;
    protected String mLastUpdateTime;

    //Change variables names to mButtonX later
    public Button b1;
    public Button b2;

    public static ArrayList<Business> restaurantList;
    public TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();

        //Button generation, initial button delcared else where
        b2 = (Button)findViewById(R.id.button3);
        b2.setVisibility(View.GONE);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pop off list
                restaurantList.remove(0);
                textView.setText("Name: " + restaurantList.get(0).name() + "\nAddress: " + restaurantList.get(0).location().address().get(0)+ "\nPhone Number: " + restaurantList.get(0).displayPhone());
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buttonStartLocationUpdate(View v) {
        Log.i(TAG, "Button Pressed");
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdate();
        }
        Log.i(TAG, "Checking Location accuracy");
        while (mCurrentLocation == null || mCurrentLocation.getAccuracy() > 1000.0) {
            Log.i(TAG, "Waiting for accuracy to reach 100 m");
            try {
                Thread.sleep(FASTEST_UPDATE_INTERVAL / 2);
                if (mCurrentLocation == null) {
                    Log.i(TAG, "Location Null");
                } else {
                    Log.i(TAG, "Accuracy = " + mCurrentLocation.getAccuracy());
                }

            } catch (InterruptedException e) {
                Log.i(TAG, "Wait got an exception");
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Passed accuracy test");
        stopLocationUpdate();
        Log.i(TAG, "Latitude: " + mCurrentLocation.getLatitude() + " Longitude: " + mCurrentLocation.getLongitude());

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            new YelpHelper().execute(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
        }

        //This button is rather out of place
        b1 = (Button)findViewById(R.id.button);
       textView = (TextView)findViewById(R.id.textView);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change activity to yelp helper
                b1.setVisibility(View.INVISIBLE);
                b2.setVisibility(View.VISIBLE);
                textView.setText("Name: " + restaurantList.get(0).name() + "\nAddress: " + restaurantList.get(0).location().address().get(0)+
                        "\nPhone Number: " + restaurantList.get(0).displayPhone() + "\nRating: " + restaurantList.get(0).rating());
            }
        });
    }

    protected void locationRequestDenied() {
        //TODO: If location access was denied then ask user to input a zip code and use the YelpHelperZipCode class instead
    }

    protected void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Requesting GPS permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION"},
                    PERMISSION_REQUEST_FINE_LOCATION);
        }
        Log.i(TAG, "Requesting Location Updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdate() {
        if (mRequestingLocationUpdates) {
            Log.i(TAG, "Stopping Location Updates");
            mRequestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdate();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdate();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Requesting GPS permissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{"android.permission.ACCESS_FINE_LOCATION"},
                        PERMISSION_REQUEST_FINE_LOCATION);
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if(mRequestingLocationUpdates) {
            startLocationUpdate();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: error code = " + connectionResult.getErrorCode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "GPS Permission Granted");
                startLocationUpdate();
            }
            else {
                Log.i(TAG, "GPS Permission Denied");
            }
        }
        Log.i(TAG, "Returning from another onRequestPermissionsResult");
    }

    public void setRestaurantList(ArrayList<Business> list){
        this.restaurantList = list;
    }
}

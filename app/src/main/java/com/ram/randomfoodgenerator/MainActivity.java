package com.ram.randomfoodgenerator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.yelp.clientlib.entities.Business;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

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
    public ImageView img;

    public static ArrayList<Business> restaurantList;
    public static ArrayList<Business> workingList;

    public TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();

        img = (ImageView)findViewById(R.id.img);
        img.setVisibility(View.GONE);

        //Button generation, initial button delcared else where
        b2 = (Button)findViewById(R.id.button3);
        b2.setVisibility(View.GONE);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pop off list
                if(workingList != null && workingList.get(0) != null) {
                    Log.i(TAG, "Retrieving next restaurant");
                    workingList.remove(0);
                    textView.setText("Name: " + workingList.get(0).name() + "\nAddress: " + workingList.get(0).location().address().get(0)+ "\nPhone Number: " + workingList.get(0).displayPhone()  + "\nRating: " + workingList.get(0).rating());
                    new DownloadImageHelper(img).execute(workingList.get(0).imageUrl());
                    if(workingList.size() == 1) {
                        Log.i(TAG, "Reached end of list, reshuffling");
                        workingList = new ArrayList<>(shuffle(restaurantList));
                    }
                }

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
            Toast.makeText(this, "Retrieving Yelp information...", Toast.LENGTH_SHORT).show();
            new YelpHelper(this).execute(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
        }
    }


    public void displayLocation() {
        b1 = (Button)findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.textView);
        b1.setVisibility(View.INVISIBLE);
        b2.setVisibility(View.VISIBLE);
        workingList = new ArrayList<>(shuffle(restaurantList));
        if(workingList != null && workingList.get(0) != null) {
            textView.setText("Name: " + workingList.get(0).name() + "\nAddress: " + workingList.get(0).location().address().get(0)+ "\nPhone Number: " + workingList.get(0).displayPhone()  + "\nRating: " + workingList.get(0).rating());
            img.setVisibility(View.VISIBLE);
            new DownloadImageHelper(img).execute(workingList.get(0).imageUrl());
        } else {
            Toast.makeText(this, "Retrieving Yelp information...", Toast.LENGTH_SHORT).show();
        }
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

    private ArrayList<Business> shuffle(ArrayList<Business> places) {
        int j = 0;
        Business temp = null;

        // Randomly swaps one index with another
        for (int i = 0; i < places.size(); i++) {
            j = ThreadLocalRandom.current().nextInt(0, i + 1);
            temp = places.get(j);
            places.set(j, places.get(i));
            places.set(i, temp);
        }
        return places;
    }
}

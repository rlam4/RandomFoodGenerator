package com.ram.randomfoodgenerator;

import android.os.AsyncTask;
import android.util.Log;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import retrofit2.Call;

/**
 * Created by Michael on 9/17/2016.
 */
public class YelpHelperZipCode extends AsyncTask<String, Void, ArrayList<Business>> {

    private static final String CONSUMER_KEY = "NVaDEK37pXM5ZGRfnN3NRg";
    private static final String CONSUMER_SECRET = "RgH-3tzg5URWf-hK8G3NA8anG-s";
    private static final String TOKEN = "V2CM0wuGT55RQ-QNzRzpLsKNZElNMThW";
    private static final String TOKEN_SECRET = "ddiuTnligH82cESsnS_TNi6xHh8";

    @Override
    protected ArrayList<Business> doInBackground(String... ZipCode) {
        YelpAPIFactory apiFactory = new YelpAPIFactory(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        YelpAPI yelpAPI = apiFactory.createAPI();

        Map<String, String> params = new HashMap<>();

        // general params
        params.put("term", "food");
        params.put("limit", "20");
        params.put("sort", "1"); // by distance

        Call<SearchResponse> call = yelpAPI.search(ZipCode[0], params);

        try {
            SearchResponse searchResponse = call.execute().body();

            return searchResponse.businesses();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Business> result) {
        Business firstRestaurant = shuffle(result).get(0);
        Log.i(MainActivity.TAG, "Retrieved restaurant: " + firstRestaurant.name() + " " + firstRestaurant.location().address() + " " + firstRestaurant.displayPhone());
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

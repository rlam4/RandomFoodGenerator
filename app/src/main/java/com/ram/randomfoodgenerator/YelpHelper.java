package com.ram.randomfoodgenerator;
/*
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;
*/
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

/**
 * Created by Abbott on 9/15/2016.
 */
public class YelpHelper {

    private static final String CONSUMER_KEY = "NVaDEK37pXM5ZGRfnN3NRg";
    private static final String CONSUMER_SECRET = "RgH-3tzg5URWf-hK8G3NA8anG-s";
    private static final String TOKEN = "V2CM0wuGT55RQ-QNzRzpLsKNZElNMThW";
    private static final String TOKEN_SECRET = "ddiuTnligH82cESsnS_TNi6xHh8";

    protected static ArrayList<Restaurant> getRestaurants(double latitude, double longitude) {
        YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET); // use this instead

        return yelpApi.queryAPI(yelpApi, latitude, longitude);
    }

    protected static ArrayList<Restaurant> getRestaurant(String zipcode) {
        YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET); // use this instead

        return yelpApi.queryAPI(yelpApi, zipcode);
    }
}

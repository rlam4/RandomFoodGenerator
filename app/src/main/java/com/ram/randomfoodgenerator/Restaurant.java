package com.ram.randomfoodgenerator;

/*
 * Model object to contain restaurant details
 */
public class Restaurant {
    public String name, address, url, image_url, display_phone, rating_img_url, mobile_url, city, postal_code, state_code;
    public Double rating, latitude, longitude;

    public Restaurant(String name, String address, String url, String image_url, String display_phone,
                      String rating_img_url, String mobile_url, String city, String postal_code,
                      String state_code, Double rating, Double latitude, Double longitude) {
        this.name = name;
        this.address = address;
        this.url = url;
        this.image_url = image_url;
        this.display_phone = display_phone;
        this.rating_img_url = rating_img_url;
        this.mobile_url = mobile_url;
        this.city = city;
        this.postal_code = postal_code;
        this.state_code = state_code;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

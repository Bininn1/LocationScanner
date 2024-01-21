package com.example.locationscanner;

import com.google.gson.annotations.SerializedName;

public class KakaoDocument {
    @SerializedName("place_name")
    String placeName;
    @SerializedName("x")
    double longitude;
    @SerializedName("y")
    double latitude;


    public String getPlaceName() {
        return placeName;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
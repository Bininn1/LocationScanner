package com.example.locationscanner;

import com.google.gson.annotations.SerializedName;

public class PlaceDocument {
    @SerializedName("place_name")
    private String placeName;

    public String getPlaceName() {
        return placeName;
    }
}
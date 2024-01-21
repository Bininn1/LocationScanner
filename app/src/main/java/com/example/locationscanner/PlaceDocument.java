package com.example.locationscanner;

import com.google.gson.annotations.SerializedName;

public class PlaceDocument {
    @SerializedName("place_name")
    private String placeName;
    // 필요한 다른 필드를 포함시키세요

    public String getPlaceName() {
        return placeName;
    }
}
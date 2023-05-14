package com.example.locationscanner;
import com.google.gson.annotations.SerializedName;

public class KakaoDocument {
    @SerializedName("place_name")
    String placeName;
    double x, y;

    public String getPlaceName() {
        return placeName;
    }
}
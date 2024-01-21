package com.example.locationscanner;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaceResult {
    @SerializedName("documents")
    private List<PlaceDocument> documents;

    public List<PlaceDocument> getDocuments() {
        return documents;
    }
}
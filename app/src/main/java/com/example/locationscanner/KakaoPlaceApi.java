package com.example.locationscanner;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface KakaoPlaceApi {
    @GET("/v2/local/search/keyword.json")
    Call<PlaceResult> getPlaceList(
            @Header("Authorization") String restApiKey,
            @Query("query") String keyword
    );
}
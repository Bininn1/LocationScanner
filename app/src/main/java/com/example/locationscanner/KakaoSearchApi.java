package com.example.locationscanner;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface KakaoSearchApi {
    @GET("/v2/local/search/keyword")
    Call<KakaoSearchResult> searchPlaces(@Query("query") String query, @Header("Authorization") String appKey);
}
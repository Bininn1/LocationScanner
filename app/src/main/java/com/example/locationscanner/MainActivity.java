package com.example.locationscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    // 지도뷰 객체 선언
    private MapView mapView;

    // 위치 매니저 객체 선언. GPS 위치, 네트워크 위치 가져오기 위함
    private LocationManager locationManager;
    private LocationListener locationListener; // 위치 리스너 객체 선언
    private EditText keywordEditText; // 검색어 EditText 선언
    private ListView placesListView; // 검색 결과 리스트뷰 객체 선언
    private Button searchButton; // 검색 버튼 선언

    private KakaoSearchApi kakaoSearchApi; // Kakao API 사용 객체 선언

    private List<String> placeNames; // 검색 결과 문자열 리스트 선언
    private ArrayAdapter<String> adapter; // 검색 결과 문자열 리스트뷰 어댑터

    // 현재 위치 버튼 객체 선언
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 현재 위치 버튼 객체 생성
        ImageButton currentLocationButton = findViewById(R.id.Currentlocation);

        mapView = findViewById(R.id.map_view);
        // 현재 위치 트래킹 모드 활성화
        mapView.setCurrentLocationTrackingMode(
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        placeNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, placeNames);

        placesListView = findViewById(R.id.placesListView);
        placesListView.setAdapter(adapter);

        keywordEditText = findViewById(R.id.keywordEditText);
        searchButton = findViewById(R.id.searchButton);

        // 검색 버튼 클릭 이벤트 처리
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPlaces();
            }
        });

        // 현재 위치 버튼 클릭 이벤트 처리
        currentLocationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 권한 체크 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            locationListener = new LocationListener() {
                // 위치 변경 시 콜백 메소드(setMapCenterPointAndZoomLevel을 이용해 해당 위치로 이동시킴.)
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    MapPoint currentLocation = MapPoint.mapPointWithGeoCoord(lat, lng);

                    // 현재 위치 중심으로 지도 화면 이동
                    mapView.setMapCenterPointAndZoomLevel(currentLocation, 2, true);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}
// 위치 공급자의 상태가 변경되었을 때 호출되는 메서드입니다. 예를 들어, 위치 공급자가 사용 가능한지 여부가 변경되었을 때 이벤트가 발생합니다.

                public void onProviderEnabled(String provider) {}
// 위치 공급자가 사용 가능한 상태로 변경되었을 때 호출되는 메서드입니다. 예를 들어, 사용자가 위치 설정을 켰을 때 이벤트가 발생합니다.

                public void onProviderDisabled(String provider) {}
// 위치 공급자가 사용 불가능한 상태로 변경되었을 때 호출되는 메서드입니다. 예를 들어, 사용자가 위치 설정을 끈 경우 이벤트가 발생합니다.

            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
// LocationManager에게 GPS 위치 공급자를 통해 위치 업데이트를 요청합니다. 1000은 업데이트 간격을 나타내며, 1은 업데이트 거리를 나타냅니다. 즉, 1미터마다 업데이트를 요청합니다.
        }

        initializeKakaoSearchApi();
// KakaoSearchApi를 초기화하는 메서드를 호출합니다. KakaoSearchApi는 Retrofit을 사용하여 카카오 검색 API와 통신하기 위한 설정을 초기화합니다.

    }

    private void getCurrentLocation() { //현재 위치를 가져옵니다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                MapPoint currentLocation = MapPoint.mapPointWithGeoCoord(lat, lng);
                mapView.setMapCenterPointAndZoomLevel(currentLocation, 2, true);
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다. 위치 서비스를 확인해주세요", Toast.LENGTH_LONG).show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
    }



    private void initializeKakaoSearchApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dapi.kakao.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        kakaoSearchApi = retrofit.create(KakaoSearchApi.class);
    }

    private void searchPlaces() {
        String keyword = keywordEditText.getText().toString();

        if (keyword.isEmpty()) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (keyword.length() < 2 || keyword.length() > 20) {
            Toast.makeText(this, "키워드의 길이는 2~20자 이내로 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Retrofit 인스턴스 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dapi.kakao.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // API 인터페이스 생성
        KakaoSearchApi kakaoSearchApi = retrofit.create(KakaoSearchApi.class);

        // API 호출
        Call<KakaoSearchResult> call = kakaoSearchApi.searchPlaces(keyword, "YOUR_API_KEY");


        kakaoSearchApi.searchPlaces(keyword, "a3256671d38bbefc3252ef48fde2cc35").enqueue(new Callback<KakaoSearchResult>() {
            @Override
            public void onResponse(Call<KakaoSearchResult> call, Response<KakaoSearchResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<KakaoDocument> documents = response.body().getDocuments();
                    updatePlacesListView(documents);
                } else {
                    Toast.makeText(MainActivity.this, "검색 중 에러가 발생했습니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<KakaoSearchResult> call, Throwable t) {
                Toast.makeText(MainActivity.this, "검색에 실패했습니다: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updatePlacesListView(List<KakaoDocument> documents) {
        placeNames.clear();
        for (KakaoDocument document : documents) {
            placeNames.add(document.getPlaceName());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        // ...
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
        // ...
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
        // ...
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
        // ...
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
    @Override
    protected void onPause() {
        super.onPause();
        // MainActivity를 중지하지 않도록 설정합니다.
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // MainActivity를 중지하지 않도록 설정합니다.
        setResult(Activity.RESULT_CANCELED);
    }
}

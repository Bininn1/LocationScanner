package com.example.locationscanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener {

    private MapView mapView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private EditText keywordEditText;
    private ListView placesListView;
    private ListView distanceListView;
    private Button searchButton;

    private KakaoSearchApi kakaoSearchApi;
    private List<String> placeNames;
    private ArrayAdapter<String> adapter, adapter2;
    private boolean isButtonClicked = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private KakaoSearchResult kakaoSearchResult;
    private List<MapPOIItem> mapPOIItems;
    private MapPOIItem previousMarker;
    private MapPOIItem initialMarker;
    private String selectedMarkerName;
    private double currentLatitude, currentLongitude, markerLatitude, markerLongitude;

    private static final String[] DISTANCE_MENU = {"100m", "200m", "300m", "400m", "500m", "600m", "700m", "800m", "900m", "1000m"};

    public static Context context_main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        // 마커 리스트 초기화
        mapPOIItems = new ArrayList<>();
        // 마커 클릭 이벤트 처리를 위해 POIItemEventListener 등록
        mapView.setPOIItemEventListener(this);

        placeNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, placeNames);

        placesListView = findViewById(R.id.placesListView);
        placesListView.setAdapter(adapter);

        adapter2 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, DISTANCE_MENU);
        distanceListView = findViewById(R.id.distanceListView);
        distanceListView.setAdapter(adapter2);

        keywordEditText = findViewById(R.id.keywordEditText);
        searchButton = findViewById(R.id.searchButton);

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPlaces();
                distanceListView.setVisibility(View.INVISIBLE);
                placesListView.setVisibility(View.VISIBLE);
                imm.hideSoftInputFromWindow(keywordEditText.getWindowToken(), 0);
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initializeLocationListener();
            getLastKnownLocation();
        }

        initializeKakaoSearchApi();

        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                placesListView.setVisibility(View.INVISIBLE); // 장소 선택 시 리스트뷰 안보임
                String selectedPlaceName = adapter.getItem(position);
                if (selectedPlaceName != null && kakaoSearchResult != null) {
                    KakaoDocument selectedPlace = kakaoSearchResult.getDocuments().get(position);
                    double lat = selectedPlace.getLatitude();
                    double lng = selectedPlace.getLongitude();
                    MapPoint selectedMapPoint = MapPoint.mapPointWithGeoCoord(lat, lng);
                    mapView.setMapCenterPoint(selectedMapPoint, true);

                    // 이전 마커 삭제
                    if (previousMarker != null) {
                        mapView.removePOIItem(previousMarker);
                    }

                    // 새로운 마커 추가
                    MapPOIItem marker = new MapPOIItem();
                    marker.setItemName(selectedPlace.getPlaceName());
                    marker.setTag(0);
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lng));
                    mapView.addPOIItem(marker);

                    // 이전 마커 변수 업데이트
                    previousMarker = marker;

                    // 첫 위치 마커 삭제
                    if (initialMarker != null) {
                        mapView.removePOIItem(initialMarker);
                        initialMarker = null;
                    }
                }
            }
        });

        distanceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = ((TextView) view).getText().toString();
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);

                // 목적지 위도 경도
                String markLat = String.valueOf(markerLatitude);
                String markLon = String.valueOf(markerLongitude);
                // 선택한 장소, 선택한 거리, 목적지 위도 경도 SecondActivity로 data 전송
                String sendData[] = {selectedMarkerName, selectedItem, markLat, markLon};
                intent.putExtra("data", sendData);

                startActivity(intent);
            }
        });


    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initializeLocationListener() {
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //위치 업데이트 이벤트 발생해도 아무 작업도 수행하지 않음
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (lastKnownLocation != null) {
            double lat = lastKnownLocation.getLatitude();
            double lng = lastKnownLocation.getLongitude();
            MapPoint currentLocation = MapPoint.mapPointWithGeoCoord(lat, lng);

            if (isButtonClicked) {
                mapView.setMapCenterPoint(currentLocation, true);
            }
            isButtonClicked = false;
        } else {
            Toast.makeText(this, "현재 위치를 가져올 수 없습니다. 위치 서비스를 확인해주세요", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeKakaoSearchApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dapi.kakao.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        kakaoSearchApi = retrofit.create(KakaoSearchApi.class);
    }

    private void searchPlaces() {
        String keyword = keywordEditText.getText().toString();
        String appKey = "KakaoAK " + "a3256671d38bbefc3252ef48fde2cc35";
        if (keyword.isEmpty()) {
            Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        // 검색 결과가 있을 때 첫 위치 마커 삭제
        if (initialMarker != null) {
            mapView.removePOIItem(initialMarker);
            initialMarker = null;
        }

        kakaoSearchApi.searchPlaces(keyword, appKey).enqueue(new Callback<KakaoSearchResult>() {
            @Override
            public void onResponse(Call<KakaoSearchResult> call, Response<KakaoSearchResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    kakaoSearchResult = response.body();
                    placeNames.clear();
                    mapView.removeAllPOIItems(); // 기존 마커들 삭제

                    for (KakaoDocument document : kakaoSearchResult.getDocuments()) {
                        placeNames.add(document.getPlaceName());
                    }

                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(MainActivity.this, "검색 결과를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KakaoSearchResult> call, Throwable t) {
                Toast.makeText(MainActivity.this, "검색에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 위치 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocationListener();
                getLastKnownLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        getCurrentLocation();
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
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        // 마커 클릭 시 발생하는 이벤트 처리
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        // 말풍선 클 시 발생하는 이벤트 처리
        distanceListView.setVisibility(View.VISIBLE); // 말풍선 클릭시 거리 리스트뷰 보여줌
        selectedMarkerName = mapPOIItem.getItemName();
        markerLatitude = mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude;
        markerLongitude = mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude;
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
    }
}
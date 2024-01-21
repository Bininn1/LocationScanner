package com.example.locationscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SecondActivity extends AppCompatActivity {
    public String lat2, lon2;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    public String loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_main);
        setTitle("LocationScanner");

        TextView location = findViewById(R.id.location);
        TextView distance = findViewById(R.id.distance);
        Button alarmStop = findViewById(R.id.alarmStop);
        Button alarmStart = findViewById(R.id.alarmStart);

        Intent intent = getIntent();
        String[] receiveData1 = intent.getStringArrayExtra("data"); // 데이터 불러옴
        loc = receiveData1[1];
        lat2 = receiveData1[2];
        lon2 = receiveData1[3];

        location.setText(receiveData1[0]); // 장소이름
        distance.setText("반경: " + receiveData1[1]); // 원하는 거리

        alarmStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SecondActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    alarmStop.setVisibility(View.VISIBLE);
                    alarmStart.setVisibility(View.GONE);
                    Toast.makeText(SecondActivity.this, "알람이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    startLocationService();
                }
            }
        });

        // 정지 버튼 누를 시 알람 꺼지고 이전 화면으로 돌아감
        alarmStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmStop.setVisibility(View.GONE);
                alarmStart.setVisibility(View.VISIBLE);
                Toast.makeText(SecondActivity.this, "알람이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                stopLocationService();
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            String sendData[] = {loc, lat2, lon2};
            intent.putExtra("position", sendData);
            startService(intent);
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
        }
    }
}
package com.example.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LocationActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate=true;

    public Button backButton;

    String mProvince;

    String mCity;

    String mCounty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_location);

        mapView=(MapView) findViewById(R.id.bmapView);
        backButton=(Button) findViewById(R.id.back_button);
        final FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permission=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(LocationActivity.this,permission,1);
        }else {
            requestLocation();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Province> provinceList=LitePal.findAll(Province.class);
                if(provinceList.size()>0){
                    for(final Province province:provinceList){
                        final StringBuilder[] str = {new StringBuilder(province.getProvinceName() + "省")};
                        if(str[0].toString().equals(mProvince)){
                            final List<City>[] cityList = new List[]{LitePal.where("provinceid = ?", String.valueOf(province.getId())).find(City.class)};
                            if(cityList[0].size()<=0){
                                String address="http://guolin.tech/api/china/"+province.getProvinceCode();
                                HttpUtil.sendOkHttpRequest(address, new Callback() {
                                    @Override
                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {//响应异常的回调函数
                                       runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LocationActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {//响应正常的回调函数
                                        String responseText=response.body().string();
                                        Utility.handleCityResponse(responseText,province.getId());
                                        cityList[0] =LitePal.where("provinceid = ?",String.valueOf(province.getId())).find(City.class);
                                        for(final City city: cityList[0]){
                                            str[0] =new StringBuilder(city.getCityName()+"市");
                                            if(str[0].toString().equals(mCity)){
                                                final List<County>[] countyList = new List[]{LitePal.where("cityid = ?", String.valueOf(city.getId())).find(County.class)};
                                                if(countyList[0].size()<=0){
                                                    String address="http://guolin.tech/api/china/"+province.getProvinceCode()+"/"+city.getCityCode();
                                                    HttpUtil.sendOkHttpRequest(address, new Callback() {
                                                        @Override
                                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {//响应异常的回调函数
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(LocationActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {//响应正常的回调函数
                                                            String responseText=response.body().string();
                                                            Utility.handleCountyResponse(responseText,city.getId());
                                                            countyList[0] =LitePal.where("cityid = ?",String.valueOf(city.getId())).find(County.class);
                                                            for(County county: countyList[0]){
                                                                str[0] =new StringBuilder(county.getCountyName()+"区");
                                                                if(str[0].toString().equals(mCounty)){
                                                                    SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(LocationActivity.this).edit();
                                                                    editor.putString("weather",null);
                                                                    editor.apply();
                                                                    Intent intent=new Intent(LocationActivity.this, WeatherActivity.class);
                                                                    intent.putExtra("weather_id",county.getWeatherId());
                                                                    startActivity(intent);
                                                                }
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    countyList[0] =LitePal.where("cityid = ?",String.valueOf(city.getId())).find(County.class);
                                                    for(County county: countyList[0]){
                                                        str[0] =new StringBuilder(county.getCountyName()+"区");
                                                        if(str[0].toString().equals(mCounty)){
                                                            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(LocationActivity.this).edit();
                                                            editor.putString("weather",null);
                                                            editor.apply();
                                                            Intent intent=new Intent(LocationActivity.this, WeatherActivity.class);
                                                            intent.putExtra("weather_id",county.getWeatherId());
                                                            startActivity(intent);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }else{
                                cityList[0] =LitePal.where("provinceid = ?",String.valueOf(province.getId())).find(City.class);
                                for(final City city: cityList[0]){
                                    str[0] =new StringBuilder(city.getCityName()+"市");
                                    if(str[0].toString().equals(mCity)){
                                        final List<County>[] countyList = new List[]{LitePal.where("cityid = ?", String.valueOf(city.getId())).find(County.class)};
                                        if(countyList[0].size()<=0){
                                            String address="http://guolin.tech/api/china/"+province.getProvinceCode()+"/"+city.getCityCode();
                                            HttpUtil.sendOkHttpRequest(address, new Callback() {
                                                @Override
                                                public void onFailure(@NotNull Call call, @NotNull IOException e) {//响应异常的回调函数
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(LocationActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {//响应正常的回调函数
                                                    String responseText=response.body().string();
                                                    Utility.handleCountyResponse(responseText,city.getId());
                                                    countyList[0] =LitePal.where("cityid = ?",String.valueOf(city.getId())).find(County.class);
                                                    for(County county: countyList[0]){
                                                        str[0] =new StringBuilder(county.getCountyName()+"区");
                                                        if(str[0].toString().equals(mCounty)){
                                                            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(LocationActivity.this).edit();
                                                            editor.putString("weather",null);
                                                            editor.apply();
                                                            Intent intent=new Intent(LocationActivity.this, WeatherActivity.class);
                                                            intent.putExtra("weather_id",county.getWeatherId());
                                                            startActivity(intent);
                                                        }
                                                    }
                                                }
                                            });
                                        }else{
                                            countyList[0] =LitePal.where("cityid = ?",String.valueOf(city.getId())).find(County.class);
                                            for(County county: countyList[0]){
                                                str[0] =new StringBuilder(county.getCountyName()+"区");
                                                if(str[0].toString().equals(mCounty)){
                                                    SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(LocationActivity.this).edit();
                                                    editor.putString("weather",null);
                                                    editor.apply();
                                                    Intent intent=new Intent(LocationActivity.this, WeatherActivity.class);
                                                    intent.putExtra("weather_id",county.getWeatherId());
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }

    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            mProvince=bdLocation.getProvince();
            mCity=bdLocation.getCity();
            mCounty=bdLocation.getDistrict();
            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation||bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();;
    }
}
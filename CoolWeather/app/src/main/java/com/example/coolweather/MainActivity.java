package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.db.SavedCounty;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;

import org.litepal.LitePal;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("isStatus",false)){
            Intent intent=new Intent(MainActivity.this, AutoUpdateService.class);
            startService(intent);
        }

        if(prefs.getString("weather",null)!=null){
            Intent intent=new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
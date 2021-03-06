package com.example.coolweather.db;

import org.litepal.crud.LitePalSupport;

public class SavedCounty extends LitePalSupport {
    private String weatherId;

    private String countyName;

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }
}

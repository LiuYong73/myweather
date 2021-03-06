package com.example.administrator.myweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weather;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}

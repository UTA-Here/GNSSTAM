package com.utahere.gnssapp;

import android.app.Application;

import com.utahere.objects.VehicleLiteObj;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tsailing on 4/26/2015.
 */
public class AppData extends Application{

    Map<Date, List<VehicleLiteObj>> vMap = new HashMap<>();
    List<Date> vDateList = new ArrayList<>();
    int allCt = 0;
    Date latestDate = new Date();
    Boolean isWifiConnected = false;
    Boolean setNightlyUpdates = false;

    //user geographical coordinates:
    double userlatitude = 0.0;
    double userlongitude = 0.0;
    double useraltitude = 0.0;
}
package com.utahere.gnssapp;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utahere.data.DBSQLiteHelper;
import com.utahere.objects.VehicleObj;
import com.utahere.objects.VehicleLiteObj;
import com.utahere.utils.SatDateUtils;
import com.utahere.log.GLog;
import com.utahere.utils.Utils;

import java.text.ParseException;
import java.util.*;

import android.os.StrictMode;

public class LocationActivity extends BaseActivity {

    private static final String TAG = LocationActivity.class.getSimpleName();

    private DBSQLiteHelper dbHelper;
    private GoogleMap myMap; // Might be null if Google Play services APK is not available.
    private SeekBar bar; // declare seekbar object variable
    String setConMed;
    String refreshStatus;
    Boolean showGPS;
    Boolean showGLONASS;

    // declare text label objects
    private TextView textAction;
    Map<Date, List<VehicleLiteObj>> vMap;
    List<Date> vDateList;
    Date latestDate;
    private Bitmap mSatelliteBitmapGPS;
    private Bitmap mSatelliteBitmapGLONASS;

    //TODO: kill activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_main);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.setConMed = prefs.getString("com.utahere.gnssapp.settings.setConversionMethodStr", "Basic");
        this.showGPS = prefs.getBoolean("com.utahere.gnssapp.settings.setShowGPSSats", true);
        this.showGLONASS = prefs.getBoolean("com.utahere.gnssapp.settings.setShowGLONASSSats", true);
        this.refreshStatus = prefs.getString("com.utahere.gnssapp.settings.setRefreshStatus", null);

        if(this.refreshStatus.equals("Running")) {
            ((TextView) findViewById(R.id.satMapHeader)).setText("Data refresh is still running");
        } else {
            ((TextView) findViewById(R.id.satMapHeader)).setText("");
        }

        long startTime1 = GLog.logStart();
        initializeVariables();
        addListenerSatelliteLocations();
        long endTime1 = System.nanoTime();
        long duration1 = (endTime1 - startTime1)/1000000; // in seconds. Divide by 1000000 to get milliseconds
        double durationS = duration1/1000.0;
        Log.d(TAG, "||||||||||||||||||||| (Time to Load: "+durationS+" secs)... ");
        GLog.logEnd(TAG, startTime1);
        //13320 rows: 11,696(11.696) | 10,911(10.911) | 11,677(11.677) | 16155(16.155) | 88142(88.142) | 9177(9.177) | 9321(9.321) | 74472(74.472)

        //Source: http://webtutsdepot.com/2011/12/03/android-sdk-tutorial-seekbar-example/
        //Source: http://examples.javacodegeeks.com/android/core/widget/seekbar/android-seekbar-example/
        //java.lang.NullPointerException
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                if(vDateList!=null && vDateList.size()>0) {
                    Date pDate = vDateList.get(progress);
                    textAction.setText(SatDateUtils.formatDateToStr(pDate));
                    setSatelliteLocations(pDate);
                }
                //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addListenerSatelliteLocations() {
        FragmentManager myFragmentManager = getFragmentManager();
        MapFragment myMapFragment
                = (MapFragment) myFragmentManager.findFragmentById(R.id.map);
        myMap = myMapFragment.getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);    //TODO: make this a setting by user
        myMap.clear();

        //Other settings: https://developers.google.com/maps/documentation/android/interactivity#zoom_controls
        UiSettings settings = myMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        myMap.setMyLocationEnabled(true);

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            setSatelliteLocations(latestDate);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "ERROR: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initializeVariables() {
        AppData ad= (AppData)getApplicationContext();
        mSatelliteBitmapGPS = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.satredicon);
        mSatelliteBitmapGLONASS = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.satblackicon);
        bar = (SeekBar) findViewById(R.id.seekBar1);

        if(ad.vMap==null || ad.vMap.size()<=0) {
            Intent refreshAct = new Intent(LocationActivity.this, RefreshActivity.class);
            refreshAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(refreshAct);
        } else {
            //initialize variables
            vMap = ad.vMap;
            vDateList = ad.vDateList;
            latestDate = ad.latestDate;

            //Set time lapse slider
            if (vDateList != null && vDateList.size() > 0) {
                //Set max based on number of time milestones in data
                bar.setMax(vMap.size() - 1);
                bar.setProgress(bar.getMax());

                textAction = (TextView) findViewById(R.id.EditTime);
                textAction.setText(SatDateUtils.formatDateToStr(vDateList.get(0)));
            }
        }
    }

    //*** SET SATELLITE LOCATIONS ***//
    //http://android-er.blogspot.fi/2013/01/move-google-maps-v2-with-auto-best-zoom.html
    private void setSatelliteLocations(Date cDate) {
        myMap.clear();

        //TODO: interpolate future position:
        /*
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uLat = prefs.getString("com.utahere.gnssapp.settings.setUserLatitude", null);
        String uLon = prefs.getString("com.utahere.gnssapp.settings.setUserLongitude", null);
        String uFut = prefs.getString("com.utahere.gnssapp.settings.setFutureDate", null);

        if(uFut!=null) {
            List<VehicleObj> vList = vMap.get(cDate);
            Log.d(TAG, "--------------------- vList: " + vList.size());
            for (int i = 0; i < vList.size(); i++) {
                VehicleObj v = vList.get(i);
                Log.d(TAG, "--------------------- date: " + v.dateStr);

                final LatLng l = new LatLng(v.latitude, v.longitude);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
                Marker marker = myMap.addMarker(new MarkerOptions()
                        .position(l)
                        .title(v.vehicleId)
                                //.snippet("lat: "+v.latitude+"\r\nlng: "+v.longitude)
                        .snippet(v.lineNum + ": " + sdf.format(v.date.getTime()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.satgreen)));
            }
        }
        */

        List<VehicleLiteObj> vList = vMap.get(cDate);
        for (int i = 0; i < vList.size(); i++) {
            VehicleLiteObj v = vList.get(i);
            if(v!=null && ((this.showGPS && v.system.equals("GPS")) || (this.showGLONASS && v.system.equals("GLONASS"))))  {
                final LatLng l = new LatLng(v.latitude, v.longitude);

                Paint paintText = new Paint();
                paintText.setColor(Color.DKGRAY);
                paintText.setShadowLayer(1, 0, 0, Color.WHITE);
                paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paintText.setTextSize(35.0f);
                paintText.setTextAlign(Paint.Align.CENTER);

                Rect boundsText = new Rect();
                paintText.getTextBounds(v.vehicleId, 0, v.vehicleId.length(),
                        boundsText);
                Bitmap icon = (v.system.equals("GPS"))?mSatelliteBitmapGPS:mSatelliteBitmapGLONASS;
                Bitmap bmpText = icon.copy(Bitmap.Config.ARGB_8888, true);

                Canvas canvasText = new Canvas(bmpText);
                canvasText.drawText(v.vehicleId, canvasText.getWidth() / 2,
                        canvasText.getHeight(), paintText);

                //https://developers.google.com/maps/documentation/android/marker
                myMap.addMarker(new MarkerOptions()
                        .position(l)
                        .title(v.vehicleId)
                        .snippet(Utils.round(v.latitude,2) + ", " + Utils.round(v.longitude,2))
                        .icon(BitmapDescriptorFactory.fromBitmap(bmpText)));
            }
        }
    }

    public void go_location(View button) throws ParseException {
        final EditText time = (EditText) findViewById(R.id.EditTime);
        Date userTime = SatDateUtils.formatStrToCal(time.getText().toString()).getTime();
        userTime = snapToNearestFifteenMins(userTime);
        time.setText(SatDateUtils.formatDateToStr(userTime));

        //Update slider to user-entered time.
        if(vDateList!=null && vDateList.size()>0) {
            boolean found = false;
            for(int i=0; i<vDateList.size(); i++) {
                if(vDateList.get(i).equals(userTime)) {
                    bar = (SeekBar) findViewById(R.id.seekBar1);
                    bar.setProgress(i);
                    found = true;
                    break;
                }
            }
            if(found) {
                setSatelliteLocations(userTime);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Date/time entered is out of range.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Intent refreshAct = new Intent(LocationActivity.this, RefreshActivity.class);
            refreshAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(refreshAct);
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(time.getWindowToken(), 0);
    }

    private double getLat(VehicleObj v) {
        double latitude = v.latitude;
        double longitude = v.longitude;

        if(this.setConMed.equals("Hirvonen")) {
            latitude = v.latitude_hirvonenMoritz;
        }
        else if(this.setConMed.equals("Torge")) {
            latitude = v.latitude_torge;
        }
        else if(this.setConMed.equals("Astro")) {
            latitude = v.latitude_astroAlmanac;
        }
        else if(this.setConMed.equals("Bowring")) {
            latitude = v.latitude_bowring;
        }

        return latitude;
    }

    private Date snapToNearestFifteenMins(Date userTime) {
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(userTime);   // assigns calendar to given date
        int mins = calendar.get(Calendar.MINUTE);
        if(mins!=0 && mins!=15 && mins!=30 && mins!=45) {
            int quotient = (mins / 15) * 15;
            int remainder = mins % 15;
            int val = (mins < 15) ? ((mins <= 7) ? 0 : 15) : ((remainder < 8) ? quotient : (quotient + 15));
            if (val <= 0) {
                val = 0;
            } else if (val >= 60) {
                val = 60;
            }
            calendar.set(Calendar.MINUTE, val);
        }
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
    }
}

package com.utahere.gnssapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.yunnanexplorer.android.ygps.DataView;
import com.yunnanexplorer.android.ygps.DateView;

public class SkyplotActivity extends BaseActivity implements GpsStatus.Listener, LocationListener {
    private static final String TAG = SkyplotActivity.class.getSimpleName();

    private LocationManager mLocationManager = null;
    SkyplotView mPositionView;
    private GPSStateView mGpsState;
    private DataView mLatitude;
    private DataView mLongitude;
    private DataView mAccuracy;
    private DataView mAltitude;
    private DataView mBearing;
    private DataView mSpeed;
    private DateView mTime;
    private DateView mDeviceTime;
    private DataView mTtff;
    private DataView mTslf;
    private DataView mSatInSky;
    private DataView mSatInFix;

    Handler mHandler;
    long mClockSkew;
    LastFixUpdater mLastFixUpdater;
    long mLastUpdateTime = -1;

    private GpsStatus mGpsStatus = null;

    //TODO: kill activity
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.skyplot_main);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String setDateRange = prefs.getString("com.utahere.gnssapp.settings.setDateRange", null);
        String setLastRefreshDate = prefs.getString("com.utahere.gnssapp.settings.setLastRefreshDate", null);
        String setRefreshStatus = prefs.getString("com.utahere.gnssapp.settings.setRefreshStatus", null);

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        mLocationManager.addGpsStatusListener(this);

        mPositionView = (SkyplotView) findViewById(R.id.positionview);

        mGpsState = (GPSStateView) findViewById(R.id.gpsstate);
        mLatitude = getField(R.id.latitude, "Latitude", "degrees");
        mLongitude = getField(R.id.longitude, "Longitude", "degrees");
        mAccuracy = getField(R.id.accuracy, "Accuracy", "m");
        mAltitude = getField(R.id.altitude, "Altitude", "m", "#.#");
        mSpeed = getField(R.id.speed, "Speed", "kmh", "#.###");
        mBearing = getField(R.id.bearing, "Bearing", "degrees");
        mTime = getDateField(R.id.time, "GPS Time", "");
        mDeviceTime = getDateField(R.id.devicetime, "Device Time", "");
        mSatInSky = getField(R.id.satinsky, "Sat in Sky","");
        mSatInFix = getField(R.id.satinfix, "Sat in Fix", "");
        mTtff = getField(R.id.ttff, "Time to first fix", "ms");
        mTslf = getField(R.id.tslf, "Time since last fix", "mmm:ss");


        mHandler = new Handler();
        mLastFixUpdater = new LastFixUpdater();
        mHandler.post(mLastFixUpdater);
    }

    protected void setGpsStatus(){
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            mGpsStatus = mLocationManager.getGpsStatus(mGpsStatus);
            Iterable<GpsSatellite> sats = mGpsStatus.getSatellites();
            int inSky = 0;
            int inFix = 0;
            for (GpsSatellite s : sats){
                inSky += 1;
                if (s.usedInFix()){
                    inFix += 1;
                }
            }
            mSatInSky.setData(inSky);
            mSatInFix.setData(inFix);
            if (inFix > 0){
                mGpsState.stateLock();
            } else {
                mGpsState.stateOn();
            }
            mTtff.setData(mGpsStatus.getTimeToFirstFix());
        } else {
            mGpsState.stateOff();
            mSatInFix.setData("no fix");
            mSatInSky.setData("gps off");
        }
        mPositionView.postInvalidate();
    }

    private DataView getField(int id, String description, String units, String format){
        DataView result = getField(id, description, units);
        result.setFormatting(format);
        return result;
    }

    private DataView getField(int id, String description, String units){
        DataView result = (DataView)findViewById(id);
        result.setDescription(description);
        result.setUnits(units);
        return result;
    }

    private DateView getDateField(int id, String description, String units){
        DateView result = (DateView)findViewById(id);
        result.setDescription(description);
        result.setUnits(units);
        return result;
    }

    //@Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            setGpsStatus();
        }
    }

    //@Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            setGpsStatus();
        }
    }

    //@Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (!LocationManager.GPS_PROVIDER.equals(provider)){
            return;
        }
        setGpsStatus();
    }

    private void updateLastUpdateTime(){
        if (mLastUpdateTime >= 0){
            long t = Math.round((System.currentTimeMillis() - mLastUpdateTime - mClockSkew) / 1000);
            long sec = t % 60;
            long min = (t / 60);
            mTslf.setData(String.format("%d:%02d", min, sec));
        }
        mDeviceTime.setData(System.currentTimeMillis());
    }

    private class LastFixUpdater implements Runnable {
        @Override
        public void run() {
            updateLastUpdateTime();
            mHandler.postDelayed(this, 1000);
        }
    }

    @Override
    public void onGpsStatusChanged(int state) {
        setGpsStatus();
    }

    private void updateLocation(Location loc){
        mLatitude.setData(loc.getLatitude());
        mLongitude.setData(loc.getLongitude());
        mAccuracy.setData(loc.getAccuracy());
        mAltitude.setData(loc.getAltitude());
        mBearing.setData(loc.getBearing());
        mSpeed.setData(loc.getSpeed());
        mTime.setData(loc.getTime());
    }

    @Override
    public void onLocationChanged(Location loc) {
        mGpsState.stateLock();
        updateLocation(loc);
        mLastUpdateTime = loc.getTime();
        mClockSkew = System.currentTimeMillis() - mLastUpdateTime;
        updateLastUpdateTime();
    }


}

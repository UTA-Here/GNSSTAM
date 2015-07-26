package com.utahere.job;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.utahere.business.SatelliteData;
import com.utahere.data.DBSQLiteHelper;
import com.utahere.gnssapp.RefreshActivity;
import com.utahere.utils.SatDateUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

/**
 * Created by Tsailing on 2/21/2015.
 */

public class IGSDataDownloadService extends IntentService {

    private static final String TAG = "IGSDataDownloadService";

    public static final String PARAM_OUT_MSG = "tmp param out msg";

    public IGSDataDownloadService() {
        super("IGSDataDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startIGSDownload();
    }

    private boolean startIGSDownload() {
        long startTime = System.nanoTime();
        boolean succeeded = false;

        DBSQLiteHelper myDBHelper = new DBSQLiteHelper(getApplicationContext());
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        String setDateRange = prefs.getString("com.utahere.gnssapp.settings.setDateRange", null);
        String gpsURL = prefs.getString("com.utahere.gnssapp.settings.setGPSURL", null);
        String gloURL = prefs.getString("com.utahere.gnssapp.settings.setGLONASSURL", null);
        Boolean onlyUpdateOnWifi = prefs.getBoolean("com.utahere.gnssapp.settings.setSchOnWifi", false);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        try {
            String nowRefreshDateStr = SatDateUtils.formatDateToStr(Calendar.getInstance().getTime());
            if((onlyUpdateOnWifi && mWifi.isConnected()) || !onlyUpdateOnWifi) {
                editor.putString("com.utahere.gnssapp.settings.setLastRefreshDate", nowRefreshDateStr);
                editor.commit();

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(RefreshActivity.ResponseReceiver.ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra(PARAM_OUT_MSG, "+Running");
                sendBroadcast(broadcastIntent);

                Log.d(TAG, "||||||||||||||||||||| (START: "+setDateRange+")... ");
                SatelliteData.getHttpSatelliteDataList(myDBHelper, Integer.parseInt(setDateRange), gpsURL, gloURL);
            } else {
                stopSelf();

                //Update status: Running
                editor.putString("com.utahere.gnssapp.settings.setRefreshStatus",
                        "ERROR("+nowRefreshDateStr+"): Refresh not executed. You are NOT on Wifi, but have selected to only update on Wifi.");
                editor.commit();
            }

            // no exceptions during parsing
            succeeded = true;
            editor.putString("com.utahere.gnssapp.settings.setRefreshStatus", "Completed");
            editor.commit();


            long endTime = System.nanoTime();
            long duration = (endTime - startTime); // Divide by 1000000 to get milliseconds

            String fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/runningTimeByDays.log";
            BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc, true));
            int allCt = myDBHelper.getVehiclesCount();
            out.write(setDateRange + " | " + allCt + " | " + duration);
            out.close();

        } catch (Exception e) {
            Log.e(TAG, "ERROR: "+e.getMessage(), e);
            e.printStackTrace();
        } finally {
            int allCt = myDBHelper.getVehiclesCount();
            Log.d(TAG, "||||||||||||||||||||| (FINISHED: "+allCt+")... ");
            editor.putString("com.utahere.gnssapp.settings.setRefreshStatus", "Completed");
            editor.commit();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(RefreshActivity.ResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(PARAM_OUT_MSG, "+Done");
            sendBroadcast(broadcastIntent);
        }

        return succeeded;
    }
}
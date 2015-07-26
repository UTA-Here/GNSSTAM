package com.utahere.gnssapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.utahere.data.DBSQLiteHelper;
import com.utahere.job.IGSDataDownloadService;

import com.utahere.log.GLog;
import com.utahere.utils.SatDateUtils;


import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Tsailing on 3/21/2015.
 */

//TODO: auto-refresh page with stats as downloads occurs

public class RefreshActivity extends BaseActivity {
    private static final String TAG = RefreshActivity.class.getSimpleName();

    private ResponseReceiver receiver;

    AppData ad;
    private DBSQLiteHelper dbHelper;
    private int allCt;
    private long dbSize;

    Boolean setShowGPSSats;
    Boolean setShowGLONASSSats;
    String setGPSURL;
    String setGLOURL;
    String setDateRange;
    String setLastRefreshDate;
    String setRefreshStatus;
    Boolean setSchOnWifi;
    String setSchTime;
    int setConMed;
    String textView2Str;

    //TODO: kill activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        GLog.logInfo(TAG, "------------------|| in RefreshActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_main);

        IntentFilter filter = new IntentFilter(RefreshActivity.ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        //Display some refresh stats to the user:
        ad = (AppData)getApplicationContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setLastRefreshDate = prefs.getString("com.utahere.gnssapp.settings.setLastRefreshDate", null);
        setRefreshStatus = prefs.getString("com.utahere.gnssapp.settings.setRefreshStatus", null);
        ad.setNightlyUpdates = (PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent("com.utahere.gnssapp.settings.setNightlyUpdates"),
                PendingIntent.FLAG_NO_CREATE) != null);
        textView2Str = "You are "+(ad.isWifiConnected?"":"NOT ")+"connected to Wifi" +
                        "\r\nNightly update " + (ad.setNightlyUpdates?"enabled":" is NOT enabled") +
                        "\r\nRefresh status: " + (setRefreshStatus==null?"-":setRefreshStatus);

        dbHelper = new DBSQLiteHelper(getApplicationContext());
        allCt = dbHelper.getVehiclesCount();
        dbSize = 0;

        try {
            dbSize = dbHelper.getDBSize();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (setRefreshStatus!=null && setRefreshStatus.equals("Running")) {
            runningStatus(prefs);
        } else {
            if (allCt <= 0) {
                newStatus(prefs);
            } else {
                stoppedStatus(prefs);
            }
        }

        Button btnStartAlarm = (Button) findViewById(R.id.btnStartAlarm);
        btnStartAlarm.setVisibility(ad.setNightlyUpdates?View.GONE:View.VISIBLE);
        Button btnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setVisibility(ad.setNightlyUpdates?View.VISIBLE:View.GONE);

        findViewById(R.id.drop_db).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBSQLiteHelper myDBHelper = new DBSQLiteHelper(getApplicationContext());
                myDBHelper.DropDB();
                droppedStatus(prefs);

                Toast.makeText(getApplicationContext(),
                        "Please wait, deleting data  ...",
                        Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad = (AppData)getApplicationContext();
                CheckBox wifi = (CheckBox) findViewById(R.id.ckwifi1);
                if(!ad.isWifiConnected && wifi.isChecked()) {
                    Toast.makeText(getApplicationContext(),
                            "ERROR: You are NOT on Wifi, but have selected to only update on Wifi.",
                            Toast.LENGTH_LONG).show();
                } else {
                    runningStatus(prefs);

                    String fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/algorithms.csv";
                    File f = new File(fileLoc);
                    f.delete();

                    Intent intent = new Intent(RefreshActivity.this, IGSDataDownloadService.class);
                    startService(intent);

                    Toast.makeText(getApplicationContext(),
                            "Please wait, starting refresh ...",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //service onDestroy callback method will be called
        findViewById(R.id.stop_Service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RefreshActivity.this, IGSDataDownloadService.class);
                stopService(intent);
                stoppedStatus(prefs);

                Toast.makeText(getApplicationContext(),
                        "Please wait, stopping refresh ...",
                        Toast.LENGTH_LONG).show();
            }
        });
    }// end onCreate

    public void startAlarm(View button) {
        EditText sch = (EditText) findViewById(R.id.EditSchTime);
        String schStr = sch.getText().toString();
        int hour = Integer.parseInt(schStr.substring(0, schStr.indexOf(":")));
        int min = Integer.parseInt(schStr.substring(schStr.indexOf(":")+1,schStr.length()));
        Calendar updateTime = Calendar.getInstance();
        updateTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        updateTime.set(Calendar.HOUR_OF_DAY, hour);
        updateTime.set(Calendar.MINUTE, min);

        Context context = getApplicationContext();
        Intent downloader = new Intent("com.utahere.gnssapp.settings.setNightlyUpdates");
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(
                Context.ALARM_SERVICE);
        //Daily alarm at specified updateTime:
        //alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
        //        updateTime.getTimeInMillis(),
        //        AlarmManager.INTERVAL_DAY, recurringDownload);

        // Test with hourly alarm
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                updateTime.getTimeInMillis(), 1*60*60*1000, recurringDownload);

        ad = (AppData)getApplicationContext();
        ad.setNightlyUpdates = (PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent("com.utahere.gnssapp.settings.setNightlyUpdates"),
                PendingIntent.FLAG_NO_CREATE) != null);

        textView2Str = "You are "+(ad.isWifiConnected?"":"NOT ")+"connected to Wifi" +
                "\r\nNightly update enabled" +
                "\r\nRefresh status: " + (setRefreshStatus==null?"-":setRefreshStatus);
        ((TextView) findViewById(R.id.textView2)).setText(textView2Str);

        Button btnStartAlarm = (Button) findViewById(R.id.btnStartAlarm);
        btnStartAlarm.setVisibility(View.GONE);
        Button btnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setVisibility(View.VISIBLE);
    }

    public void cancelAlarm(View button) {
        Context context = getApplicationContext();
        Intent intent = new Intent("com.utahere.gnssapp.settings.setNightlyUpdates");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        ad = (AppData)getApplicationContext();
        ad.setNightlyUpdates = (PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent("com.utahere.gnssapp.settings.setNightlyUpdates"),
                PendingIntent.FLAG_NO_CREATE) != null);

        textView2Str = "You are "+(ad.isWifiConnected?"":"NOT ")+"connected to Wifi" +
                "\r\nNightly update is NOT enabled" +
                "\r\nRefresh status: " + (setRefreshStatus==null?"-":setRefreshStatus);
        ((TextView) findViewById(R.id.textView2)).setText(textView2Str);

        Button btnStartAlarm = (Button) findViewById(R.id.btnStartAlarm);
        btnStartAlarm.setVisibility(View.VISIBLE);
        Button btnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setVisibility(View.GONE);
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.utahere.gnssapp.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(IGSDataDownloadService.PARAM_OUT_MSG);

            if(text.indexOf("+Done")==0) {
                stoppedStatus(null);
            } else if (text.equals("+Running")) {
                //BOTTOM TEXT:
                TextView refresh_stats = (TextView) findViewById(R.id.refresh_stats);
                refresh_stats.setText("Last refresh date: " + (setLastRefreshDate == null ? "never" : setLastRefreshDate) +
                        "\r\nRows in DB: processing .." +
                        "\r\nDate range cached: " + setDateRange + " GPS weeks" +
                        "\r\nData size: processing ..");

            }
        }
    }

    public void saveSettings(View button) {
        Boolean hasErr = false;
        final CheckBox wifi = (CheckBox) findViewById(R.id.ckwifi1);
        final EditText sch = (EditText) findViewById(R.id.EditSchTime);
        final EditText his = (EditText) findViewById(R.id.EditDateRange);
        int hisInt = Integer.parseInt(his.getText().toString());
        if(hisInt<=0) {
            hasErr = true;
            Toast.makeText(getApplicationContext(),
                    "ERROR: Invalid number entered for Historic.",
                    Toast.LENGTH_LONG).show();
        } else if(hisInt>4) {
            hasErr = true;
            Toast.makeText(getApplicationContext(),
                    "ERROR: 4 GPS weeks is the maximum allowed data cache.",
                    Toast.LENGTH_LONG).show();
        }

        final CheckBox gps = (CheckBox) findViewById(R.id.ckgps1);
        final CheckBox glo = (CheckBox) findViewById(R.id.ckglo1);
        final EditText gpsurl = (EditText) findViewById(R.id.EditGPSURL);
        final EditText glourl = (EditText) findViewById(R.id.EditGLOURL);

        if(!gps.isChecked() && !glo.isChecked()) {
            hasErr = true;
            Toast.makeText(getApplicationContext(),
                    "ERROR: You must select at least one satellite system!",
                    Toast.LENGTH_LONG).show();
        }

        //Update settings.
        if(!hasErr) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("com.utahere.gnssapp.settings.setSchOnWifi", wifi.isChecked());
            editor.putString("com.utahere.gnssapp.settings.setSchTime", sch.getText().toString());
            editor.putString("com.utahere.gnssapp.settings.setDateRange", his.getText().toString());
            editor.putBoolean("com.utahere.gnssapp.settings.setShowGPSSats", gps.isChecked());
            editor.putBoolean("com.utahere.gnssapp.settings.setShowGLONASSSats", glo.isChecked());
            editor.putString("com.utahere.gnssapp.settings.setGPSURL", gpsurl.getText().toString());
            editor.putString("com.utahere.gnssapp.settings.setGLONASSURL", glourl.getText().toString());
            editor.commit();

            Toast.makeText(getApplicationContext(),
                    "Settings saved!",
                    Toast.LENGTH_LONG).show();

            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(sch.getWindowToken(), 0);
        }
    }

    private void newStatus(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        setContentView(R.layout.refresh_main);

        //TOP TEXT:
        textView2Str = textView2Str + "\r\nYour database is empty. " +
                "Click on the \"Start Refresh\" button to download satellite data from igscb.jpl.nasa.gov.";
        ((TextView) findViewById(R.id.textView2)).setText(textView2Str);

        //BUTTONS:
        Button dropDBButton = (Button) findViewById(R.id.drop_db);
        dropDBButton.setVisibility(View.GONE);
        Button startSrvDBButton = (Button) findViewById(R.id.start_service);
        startSrvDBButton.setVisibility(View.VISIBLE);
        Button stopSrvDBButton = (Button) findViewById(R.id.stop_Service);
        stopSrvDBButton.setVisibility(View.GONE);
        Button btnStartAlarm = (Button) findViewById(R.id.btnStartAlarm);
        btnStartAlarm.setVisibility(ad.setNightlyUpdates?View.GONE:View.VISIBLE);
        Button btnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setVisibility(ad.setNightlyUpdates?View.VISIBLE:View.GONE);
        setSettingsFields(prefs);

        //BOTTOM TEXT:
    }

    private void runningStatus(SharedPreferences prefs) {
        if(prefs==null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        SharedPreferences.Editor editor = prefs.edit();
        ad = (AppData)getApplicationContext();
        GLog.logInfo(TAG, "------------------|| in runningStatus");
        setContentView(R.layout.refresh_main);

        //UPDATE STATUS:
        editor.putString("com.utahere.gnssapp.settings.setRefreshStatus", "Running");
        editor.commit();

        //TOP TEXT:
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setText("You are "+(ad.isWifiConnected?"":"NOT ")+"connected to Wifi" +
                        "\r\nNightly update " + (ad.setNightlyUpdates?"enabled":" is NOT enabled") +
                        "\r\nRefresh status: Running");

        //BUTTONS:
        Button dropDBButton = (Button) findViewById(R.id.drop_db);
        dropDBButton.setVisibility(View.GONE);
        Button startSrvDBButton = (Button) findViewById(R.id.start_service);
        startSrvDBButton.setVisibility(View.GONE);
        Button stopSrvDBButton = (Button) findViewById(R.id.stop_Service);
        stopSrvDBButton.setVisibility(View.VISIBLE);
        Button btnStartAlarm = (Button) findViewById(R.id.btnStartAlarm);
        btnStartAlarm.setVisibility(ad.setNightlyUpdates?View.GONE:View.VISIBLE);
        Button btnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setVisibility(ad.setNightlyUpdates?View.VISIBLE:View.GONE);
        setSettingsFields(prefs);

        //BOTTOM TEXT:
        TextView refresh_stats = (TextView) findViewById(R.id.refresh_stats);
        refresh_stats.setText("Last refresh date: " + (setLastRefreshDate == null ? "never" : setLastRefreshDate) +
                        "\r\nRows in DB: " + allCt +
                        "\r\nDate range cached: " + setDateRange + " GPS weeks" +
                        "\r\nData size: " + ((dbSize < 1000000) ? "<1" : (dbSize / 1000000)) + " MB"
        );
    }

    private void droppedStatus(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        ad = (AppData)getApplicationContext();
        GLog.logInfo(TAG, "------------------|| in droppedStatus");
        setContentView(R.layout.refresh_main);

        //UPDATE STATUS:
        editor.putString("com.utahere.gnssapp.settings.setLastRefreshDate", null);
        editor.putString("com.utahere.gnssapp.settings.setRefreshStatus", "DB dropped on "+ SatDateUtils.formatDateToStr(Calendar.getInstance().getTime()));
        editor.commit();

        //TOP TEXT:
        ((TextView) findViewById(R.id.textView2)).setText(
                        "You are "+(ad.isWifiConnected?"":"NOT ")+"connected to Wifi" +
                        "\r\nNightly update " + (ad.setNightlyUpdates?"enabled":" is NOT enabled") +
                        "\r\nAll data deleted.");

        //BUTTONS:
        Button dropDBButton = (Button) findViewById(R.id.drop_db);
        dropDBButton.setVisibility(View.GONE);
        Button startSrvDBButton = (Button) findViewById(R.id.start_service);
        startSrvDBButton.setVisibility(View.VISIBLE);
        Button stopSrvDBButton = (Button) findViewById(R.id.stop_Service);
        stopSrvDBButton.setVisibility(View.GONE);
        Button btnStartAlarm = (Button) findViewById(R.id.btnStartAlarm);
        btnStartAlarm.setVisibility(ad.setNightlyUpdates?View.GONE:View.VISIBLE);
        Button btnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setVisibility(ad.setNightlyUpdates?View.VISIBLE:View.GONE);
        setSettingsFields(prefs);

        //BOTTOM TEXT:
        //http://tekeye.biz/2013/android-app-text-not-updating
        ((TextView) findViewById(R.id.textView2)).setText(
                "You are " + (ad.isWifiConnected ? "" : "NOT ") + "connected to Wifi" +
                        "\r\nNightly update " + (ad.setNightlyUpdates?"enabled":" is NOT enabled") +
                        "\r\nAll data purged. " +
                        "Click on the \"Start Refresh\" button to download satellite data from igscb.jpl.nasa.gov.");
    }

    private void stoppedStatus(SharedPreferences prefs) {
        if(prefs==null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        SharedPreferences.Editor editor = prefs.edit();
        ad = (AppData)getApplicationContext();
        GLog.logInfo(TAG, "------------------|| in stoppedStatus");
        setContentView(R.layout.refresh_main);

        //UPDATE STATUS:
        if(!setRefreshStatus.equals("Completed")) {
            editor.putString("com.utahere.gnssapp.settings.setRefreshStatus", "Stopped");
            editor.commit();
        }

        //TOP TEXT:
        ((TextView) findViewById(R.id.textView2)).setText(
                "You are "+(ad.isWifiConnected?"":"NOT ")+"connected to Wifi" +
                "\r\nNightly update " + (ad.setNightlyUpdates?"enabled":" is NOT enabled") +
                "\r\nRefresh stopped");

        //BUTTONS:
        Button dropDBButton = (Button) findViewById(R.id.drop_db);
        dropDBButton.setVisibility(View.VISIBLE);
        Button startSrvDBButton = (Button) findViewById(R.id.start_service);
        startSrvDBButton.setVisibility(View.VISIBLE);
        Button stopSrvDBButton = (Button) findViewById(R.id.stop_Service);
        stopSrvDBButton.setVisibility(View.GONE);
        Button btnStartAlarm = (Button) findViewById(R.id.btnStartAlarm);
        btnStartAlarm.setVisibility(ad.setNightlyUpdates?View.GONE:View.VISIBLE);
        Button btnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setVisibility(ad.setNightlyUpdates?View.VISIBLE:View.GONE);
        setSettingsFields(prefs);

        //BOTTOM TEXT:
        TextView refresh_stats = (TextView) findViewById(R.id.refresh_stats);
        refresh_stats.setText("Last refresh date: " + (setLastRefreshDate == null ? "never" : setLastRefreshDate) +
                        "\r\nRows in DB: " + allCt +
                        "\r\nDate range cached: " + setDateRange + " GPS weeks" +
                        "\r\nData size: " + ((dbSize < 1000000) ? "<1" : (dbSize / 1000000)) + " MB"
        );
    }

    private void setSettingsFields(SharedPreferences prefs) {
        setSchOnWifi = prefs.getBoolean("com.utahere.gnssapp.settings.setSchOnWifi", true);
        setSchTime = prefs.getString("com.utahere.gnssapp.settings.setSchTime", null);
        setDateRange = prefs.getString("com.utahere.gnssapp.settings.setDateRange", null);

        final CheckBox wifi = (CheckBox) findViewById(R.id.ckwifi1);
        if(wifi!=null && setSchOnWifi!=null) wifi.setChecked(setSchOnWifi);
        final EditText sch = (EditText) findViewById(R.id.EditSchTime);
        if(sch!=null && setSchTime!=null) sch.setText(setSchTime);
        final EditText his = (EditText) findViewById(R.id.EditDateRange);
        if(his!=null && setDateRange!=null) his.setText(setDateRange);

        setShowGPSSats = prefs.getBoolean("com.utahere.gnssapp.settings.setShowGPSSats", true);
        setShowGLONASSSats = prefs.getBoolean("com.utahere.gnssapp.settings.setShowGLONASSSats", true);
        setGPSURL = prefs.getString("com.utahere.gnssapp.settings.setGPSURL", null);
        setGLOURL = prefs.getString("com.utahere.gnssapp.settings.setGLONASSURL", null);

        final CheckBox gps = (CheckBox) findViewById(R.id.ckgps1);
        final CheckBox glo = (CheckBox) findViewById(R.id.ckglo1);
        final EditText gpsurl = (EditText) findViewById(R.id.EditGPSURL);
        final EditText glourl = (EditText) findViewById(R.id.EditGLOURL);
        if(gps!=null && setShowGPSSats!=null) gps.setChecked(setShowGPSSats);
        if(glo!=null && setShowGLONASSSats!=null) glo.setChecked(setShowGLONASSSats);
        if(gpsurl!=null && setGPSURL!=null) gpsurl.setText(setGPSURL);
        if(glourl!=null && setGLOURL!=null) glourl.setText(setGLOURL);

    }
}
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.utahere.gnssapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.utahere.data.DBSQLiteHelper;
import com.utahere.log.GLog;
import com.utahere.objects.VehicleLiteObj;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * This sample shows you how to use ActionBarCompat to create a basic Activity which displays
 * action items. It covers inflating items from a menu resource, as well as adding an item in code.
 * <p/>
 * This Activity extends from {@link ActionBarActivity}, which provides all of the function
 * necessary to display a compatible Action Bar on devices running Android v2.1+.
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    String setDateRange;

    //A ProgressDialog object
    private ProgressDialog progressDialog;
    private DBSQLiteHelper dbHelper;
    private int vCt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "------------------|| in MainActivity.onCreate");

        Thread.currentThread().setContextClassLoader(MainActivity.class.getClassLoader());

        Thread.currentThread().setContextClassLoader(new ClassLoader() {
            @Override
            public Enumeration<URL> getResources(String resName) throws IOException {
                Log.i("-.-DEBUG-.-", "Stack trace of who uses " +
                        "Thread.currentThread().getContextClassLoader()." +
                        "getResources(String resName):", new Exception());
                return super.getResources(resName);
            }
        });

        super.onCreate(savedInstanceState);

        checkRefresh();
    }

    private void checkRefresh() {
        //Check if it's user's first time:
        //Source: http://stackoverflow.com/questions/15385117/save-little-information-like-settings-in-android
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        setDateRange = settings.getString("com.utahere.gnssapp.settings.setDateRange", null);
        boolean promptRefresh = true;
        if(!settings.getBoolean("isFirstRun", true)) {
            //Source: http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
            dbHelper = new DBSQLiteHelper(this);

            try {
                vCt = dbHelper.getVehiclesCount();
            } catch (android.database.sqlite.SQLiteException e) {
                e.printStackTrace();
            }

            if(vCt > 0) {
                promptRefresh = false;
                AppData ad= (AppData)getApplicationContext();
                ad.allCt = vCt;

                //Initialize a LoadViewTask object and call the execute() method
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)
                    new LoadViewTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    new LoadViewTask().execute();
            }
        } else {
            //Set defaults for first timers:
            PreferenceManager.setDefaultValues(this, R.xml.settings_prefs, false);
        }
        SharedPreferences.Editor edit= settings.edit();
        edit.putBoolean("isFirstRun", false);
        edit.commit();

        if(promptRefresh) {
            Intent refreshAct = new Intent(this, RefreshActivity.class);
            refreshAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(refreshAct);
        }
    }

    //Source: http://www.41post.com/4588/programming/android-coding-a-loading-screen-part-1
    //To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
        //Before running code in separate thread
        @Override
        protected void onPreExecute()
        {
            //Create a new progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("Initializing GNSS App");
            progressDialog.setMessage("Please wait...");
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            //The maximum number is the total number of records to load
            progressDialog.setMax(vCt);
            //Set the current progress to 0
            progressDialog.setProgress(0);
            //Display the progress dialog
            progressDialog.show();
        }

        //The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params)
        {
            AppData ad = (AppData)getApplicationContext();

            /* This is just a code that delays the thread execution 4 times,
             * during 850 milliseconds and updates the current progress. This
             * is where the code that is going to be executed on a background
             * thread must be placed.
             */
            try
            {
                //Get the current thread's token
                synchronized (this)
                {
                    //*** GET SATELLITE POSITIONS FROM DATABASE ***//
                    if(ad.vMap==null ||ad.vMap.size()<=0) {
                        progressDialog.setMessage("Loading data...");
                        dbHelper = new DBSQLiteHelper(MainActivity.this);
                        long startTime = GLog.logStart();
                        int hisInt = Integer.parseInt(setDateRange);
                        Map<Date, List<VehicleLiteObj>> vMap = dbHelper.getVehiclesLiteMapWithProgressDialog(progressDialog, vCt, hisInt);

                        GLog.logEnd(TAG+"-getVehiclesMapWithProgressDialog()", startTime);
                        //141,151(141.151)
                        //20,287(20.287) - without PositionCalculations
                        //13,320 rows without PositionCalculations - 11,586(11.586) | 10,794(10.794) | 11,561(11.561) | 87511(87.511) | 9080(9.08) | 9211(9.211)
                        //73924(73.924)

                        ad.vMap = vMap;

                        //Sort data and categorize by date
                        List<Date> vDateList = new ArrayList<>();
                        Date latestDate = null;
                        for (Map.Entry<Date, List<VehicleLiteObj>> entry : ad.vMap.entrySet()) {
                            Date d = entry.getKey();
                            if (latestDate == null || latestDate.getTime() < d.getTime()) {
                                latestDate = d;
                            }
                            vDateList.add(d);
                        }
                        Collections.sort(vDateList);
                        ad.vDateList = vDateList;
                        ad.latestDate = latestDate;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        //Update the progress
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            //set the current progress of the progress dialog
            progressDialog.setProgress(values[0]);
        }

        //after executing the code in the thread
        @Override
        protected void onPostExecute(Void result)
        {
            //close the progress dialog
            progressDialog.dismiss();
            //initialize the View
            Intent locAct = new Intent(MainActivity.this, LocationActivity.class);
            locAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(locAct);
        }
    }
}

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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.utahere.data.DBSQLiteHelper;

/**
 * This sample shows you how to use ActionBarCompat to create a basic Activity which displays
 * action items. It covers inflating items from a menu resource, as well as adding an item in code.
 * <p/>
 * This Activity extends from {@link ActionBarActivity}, which provides all of the function
 * necessary to display a compatible Action Bar on devices running Android v2.1+.
 */
public class BaseActivity extends ActionBarActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        AppData ad= (AppData)getApplicationContext();
        ad.isWifiConnected = mWifi.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // It is also possible add items here. Use a generated id from
        // resources (ids.xml) to ensure that all menu ids are distinct.
        MenuItem locationItem = menu.add(0, R.id.menu_location, 0, R.string.menu_location);
        locationItem.setIcon(R.drawable.ic_action_location);
        MenuItemCompat.setShowAsAction(locationItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItem skyplotItem = menu.add(0, R.id.menu_skyplot, 0, R.string.menu_skyplot);
        skyplotItem.setIcon(R.drawable.ic_action_skyplot);
        MenuItemCompat.setShowAsAction(skyplotItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItem testcalcItem = menu.add(0, R.id.menu_testcalc, 0, R.string.menu_testcalc);
        testcalcItem.setIcon(R.drawable.ic_action_testcalc);
        MenuItemCompat.setShowAsAction(testcalcItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

        // Inflate our menu from the resources by using the menu inflater.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }
    // END_INCLUDE(create_menu)

    // BEGIN_INCLUDE(menu_item_selected)

    /**
     * This method is called when one of the menu items to selected. These items
     * can be on the Action Bar, the overflow menu, or the standard options menu. You
     * should return true if you handle the selection.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_location:
                AppData ad= (AppData)getApplicationContext();
                DBSQLiteHelper dbHelper = new DBSQLiteHelper(getApplicationContext());
                int allCt = dbHelper.getVehiclesCount();
                if(((ad.vMap==null || ad.vMap.size()<=0) && allCt>0) || (ad.vMap!=null && allCt!=ad.allCt)) {
                    Intent mainAct = new Intent(this, MainActivity.class);
                    mainAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(mainAct);
                } else if(ad.vMap==null || ad.vMap.size()<=0) {
                    Toast.makeText(getApplicationContext(),
                            "ERROR: you must download satellite data first!",
                            Toast.LENGTH_LONG).show();

                    Intent refreshAct = new Intent(this, RefreshActivity.class);
                    refreshAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(refreshAct);
                } else {
                    Intent locAct = new Intent(this, LocationActivity.class);
                    locAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(locAct);
                }
                return true;

            case R.id.menu_skyplot:
                Intent skyAct = new Intent(this, SkyplotActivity.class);
                skyAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(skyAct);
                return true;

            case R.id.menu_testcalc:
                Intent testCalc = new Intent(this, TestCalculatorActivity.class);
                testCalc.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(testCalc);
                return true;

            case R.id.menu_refresh:
                Intent refreshAct = new Intent(getApplicationContext(), RefreshActivity.class);
                refreshAct.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(refreshAct);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE(menu_item_selected)
}

package com.utahere.job;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DailyAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "DailyAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Recurring alarm; requesting download service.");
        // start the download
        Intent downloader = new Intent(context, IGSDataDownloadService.class);
        context.startService(downloader);
    }
}

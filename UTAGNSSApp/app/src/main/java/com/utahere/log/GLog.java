package com.utahere.log;

/**
 * Created by sailing on 5/3/2015.
 */
public class GLog {
    static boolean isLogStopwatchActive = true;
    static boolean isLogConfirmActive = true;
    static boolean isLogInfoActive = true;
    static boolean isLogWarningActive = true;
    static boolean isLogErrActive = true;
    static boolean isLogFatalActive = true;

    public static long logStart() {
        return System.nanoTime();
    }

    public static void logEnd(String TAG, long startTime) {
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000; // in seconds. Divide by 1000000 to get milliseconds
        double durationS = duration/1000.0;
        if(isLogStopwatchActive) {
            android.util.Log.d(TAG, "||||||||||||||||||| duration: " + duration + "(" + durationS + ")");
        }
    }

    public static void logConfirm(String TAG, String message) {
        if(isLogConfirmActive) android.util.Log.d(TAG, "(CONFIRM) " + message);
    }

    public static void logInfo(String TAG, String message) {
        if(isLogInfoActive) android.util.Log.d(TAG, "(INFO) " + message);
    }

    public static void logWarning(String TAG, String message) {
        if(isLogWarningActive) android.util.Log.d(TAG, "(WARNING) " + message);
    }

    public static void logErr(String TAG, String message) {
        if(isLogErrActive) android.util.Log.d(TAG, "(ERROR) " + message);
    }

    public static void logFatal(String TAG, String message) {
        if(isLogFatalActive) android.util.Log.d(TAG, "(FATAL) " + message);
    }
}

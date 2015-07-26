package com.utahere.data;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.utahere.log.GLog;
import com.utahere.objects.VehicleLiteObj;
import com.utahere.objects.VehicleObj;
import com.utahere.utils.SatDateUtils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tsailing on 3/19/2015.
 */

//Source: http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
public class DBSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = DBSQLiteHelper.class.getSimpleName();

    //Vehicle table
    public static final String TABLE_VEHICLE = "vehicle";
    public static final String COLUMN_VEH_ID = "_id";
    public static final String COLUMN_VEH_FILENAME = "filename";
    public static final String COLUMN_VEH_GPSWEEK = "gpsweek";
    public static final String COLUMN_VEH_VID = "vid";
    public static final String COLUMN_VEH_DATE = "date";
    public static final String COLUMN_VEH_X = "x";
    public static final String COLUMN_VEH_Y = "y";
    public static final String COLUMN_VEH_Z = "z";
    public static final String COLUMN_VEH_LATITUDE = "latitude";
    public static final String COLUMN_VEH_LONGITUDE = "longitude";
    public static final String COLUMN_VEH_LATITUDE_HMM = "latitude_hirvonenMoritz";
    public static final String COLUMN_VEH_LATITUDE_TM = "latitude_torge";
    public static final String COLUMN_VEH_LATITUDE_AAM = "latitude_astroAlmanac";
    public static final String COLUMN_VEH_LATITUDE_BWM = "latitude_bowring";
    public static final String COLUMN_VEH_DEBUG = "debug";
    public static final String COLUMN_VEH_DATELONG = "datelong";
    public static final String COLUMN_VEH_HEALTH = "health";
    public static final String COLUMN_VEH_SYSTEM = "system";

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "utagnss";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_VEHICLE + "("
            + COLUMN_VEH_ID + " integer primary key autoincrement, "
            + COLUMN_VEH_FILENAME + " text not null, "
            + COLUMN_VEH_GPSWEEK + " integer not null, "
            + COLUMN_VEH_VID + " text not null, "
            + COLUMN_VEH_DATE + " text not null, "
            + COLUMN_VEH_X + " double not null, "
            + COLUMN_VEH_Y + " double not null, "
            + COLUMN_VEH_Z + " double not null, "
            + COLUMN_VEH_LATITUDE + " double not null, "
            + COLUMN_VEH_LONGITUDE + " double not null, "
            + COLUMN_VEH_LATITUDE_HMM + " double not null, "
            + COLUMN_VEH_LATITUDE_TM + " double not null, "
            + COLUMN_VEH_LATITUDE_AAM + " double not null, "
            + COLUMN_VEH_LATITUDE_BWM + " double not null, "
            + COLUMN_VEH_DEBUG + " text, "
            + COLUMN_VEH_DATELONG + " integer not null,"
            + COLUMN_VEH_HEALTH + " integer not null,"
            + COLUMN_VEH_SYSTEM + " text not null"
            + ")";

    public DBSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLE);
        onCreate(db);
    }

    public void DropDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLE);
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new record
    public void addVehicle(VehicleObj v) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(DBSQLiteHelper.COLUMN_VEH_FILENAME, v.fileName);
        values.put(DBSQLiteHelper.COLUMN_VEH_GPSWEEK, v.gpsWeek);
        values.put(DBSQLiteHelper.COLUMN_VEH_VID, v.vehicleId);
        values.put(DBSQLiteHelper.COLUMN_VEH_DATE, v.dateStr);
        values.put(DBSQLiteHelper.COLUMN_VEH_X, v.xcoordinate);
        values.put(DBSQLiteHelper.COLUMN_VEH_Y, v.ycoordinate);
        values.put(DBSQLiteHelper.COLUMN_VEH_Z, v.zcoordinate);
        values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE, v.latitude);
        values.put(DBSQLiteHelper.COLUMN_VEH_LONGITUDE, v.longitude);
        values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_HMM, v.latitude_hirvonenMoritz);
        values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_TM, v.latitude_torge);
        values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_AAM, v.latitude_astroAlmanac);
        values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_BWM, v.latitude_bowring);
        values.put(DBSQLiteHelper.COLUMN_VEH_DEBUG, v.debug);
        values.put(DBSQLiteHelper.COLUMN_VEH_DATELONG, v.dateLong);
        values.put(DBSQLiteHelper.COLUMN_VEH_HEALTH, v.health);
        values.put(DBSQLiteHelper.COLUMN_VEH_SYSTEM, v.system);

        // Inserting Row
        db.insert(TABLE_VEHICLE, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close(); // Closing database connection
    }

    //Source: http://www.outofwhatbox.com/blog/2010/12/android-using-databaseutils-inserthelper-for-faster-insertions-into-sqlite-database/
    public void addVehicleList(List<VehicleObj> vList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new ContentValues();
        for (int i = 0; i < vList.size(); i++) {
            VehicleObj v = vList.get(i);
            // Add the data for each column
            values.put(DBSQLiteHelper.COLUMN_VEH_FILENAME, v.fileName);
            values.put(DBSQLiteHelper.COLUMN_VEH_GPSWEEK, v.gpsWeek);
            values.put(DBSQLiteHelper.COLUMN_VEH_VID, v.vehicleId);
            values.put(DBSQLiteHelper.COLUMN_VEH_DATE, v.dateStr);
            values.put(DBSQLiteHelper.COLUMN_VEH_X, v.xcoordinate);
            values.put(DBSQLiteHelper.COLUMN_VEH_Y, v.ycoordinate);
            values.put(DBSQLiteHelper.COLUMN_VEH_Z, v.zcoordinate);
            values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE, v.latitude);
            values.put(DBSQLiteHelper.COLUMN_VEH_LONGITUDE, v.longitude);
            values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_HMM, v.latitude_hirvonenMoritz);
            values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_TM, v.latitude_torge);
            values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_AAM, v.latitude_astroAlmanac);
            values.put(DBSQLiteHelper.COLUMN_VEH_LATITUDE_BWM, v.latitude_bowring);
            values.put(DBSQLiteHelper.COLUMN_VEH_DEBUG, v.debug);
            values.put(DBSQLiteHelper.COLUMN_VEH_DATELONG, v.dateLong);
            values.put(DBSQLiteHelper.COLUMN_VEH_HEALTH, v.health);
            values.put(DBSQLiteHelper.COLUMN_VEH_SYSTEM, v.system);

            // Insert the row into the database.
            db.insert(TABLE_VEHICLE, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close(); // Closing database connection
    }

    // Getting all records
    public List<VehicleObj> getAllVehicles() throws ParseException{
        List<VehicleObj> vList = new ArrayList<VehicleObj>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_VEHICLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                VehicleObj v = new VehicleObj();
                v.setDBId(cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_ID)));
                v.setFileName(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_FILENAME)));
                v.setGpsWeek(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_GPSWEEK)));
                v.setVehicleId(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_VID)));
                v.setDateStr(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATE)));
                v.setXcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_X)));
                v.setYcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Y)));
                v.setZcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Z)));
                v.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE)));
                v.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LONGITUDE)));
                v.setLatitude_hirvonenMoritz(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_HMM)));
                v.setLatitude_torge(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_TM)));
                v.setLatitude_astroAlmanac(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_AAM)));
                v.setLatitude_bowring(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_BWM)));
                v.setDebug(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DEBUG)));
                v.setDateLong(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATELONG)));
                v.setHealth(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_HEALTH)));
                v.setSystem(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_SYSTEM)));
                // Adding vehicle obj to list
                vList.add(v);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection

        // return list
        return vList;
    }

    public long getDBSize() throws ParseException {
        SQLiteDatabase db = this.getWritableDatabase();
        long ct = new File(db.getPath()).length();
        db.close(); // Closing database connection
        return ct;
    }

    public Map<Date, List<VehicleLiteObj>> getVehiclesLiteMapWithProgressDialog(
            ProgressDialog p, int vCt,
            int setDateRange
    ) throws ParseException{
        String selectQuery =
                "Select * FROM " + DBSQLiteHelper.TABLE_VEHICLE;
        if(setDateRange>0) {
            //Select latest date, then query subtract number of days from it.
            long latestDate = getLatestDate();
            long getRange = latestDate - SatDateUtils.formatDaysToLong(setDateRange);

            selectQuery = selectQuery + " where " + DBSQLiteHelper.COLUMN_VEH_DATELONG + " >= " + getRange;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        Map<Date, List<VehicleLiteObj>> vMap = new HashMap<>();
        List<VehicleLiteObj> vList = null;
        long startTime1 = System.nanoTime();
        int counter = 0;
        int increment = vCt/10;
        int CtInc = increment;
        if (cursor.moveToFirst()) {
            do {
                long dateLong = cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATELONG));
                Date d = SatDateUtils.formatLongToDate(dateLong);
                vList = vMap.get(d);
                if(vList==null) {
                    vList = new ArrayList<VehicleLiteObj>();
                }
                VehicleLiteObj v = new VehicleLiteObj();

                v.setVehicleId(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_VID)));
                v.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE)));
                v.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LONGITUDE)));
                v.setSystem(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_SYSTEM)));
                vList.add(v);
                vMap.put(d, vList);

                counter++;
                if(counter >= CtInc) {
                    p.incrementProgressBy(increment);
                    CtInc = CtInc + increment;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        GLog.logEnd(TAG, startTime1);
        //141106(141.106)
        //for 13320 DB rows: 11,580(11.58) |  10,789(10.789) | 11,556(11.556) | 87475(87.475) | 9075(9.075) | 9206(9.206) | 73870(73.87)

        return vMap;
    }

    public Map<Date, List<VehicleObj>> getVehiclesMapWithProgressDialog(
            ProgressDialog p, int vCt,
            int setDateRange
    ) throws ParseException{
        String selectQuery =
                "Select * FROM " + DBSQLiteHelper.TABLE_VEHICLE;
        if(setDateRange>0) {
            //Select latest date, then query subtract number of days from it.
            long latestDate = getLatestDate();
            long getRange = latestDate - SatDateUtils.formatDaysToLong(setDateRange);

            selectQuery = selectQuery + " where " + DBSQLiteHelper.COLUMN_VEH_DATELONG + " >= " + getRange;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        Map<Date, List<VehicleObj>> vMap = new HashMap<>();
        List<VehicleObj> vList = null;
        long startTime1 = System.nanoTime();
        int counter = 0;
        int increment = vCt/10;
        int CtInc = increment;
        if (cursor.moveToFirst()) {
            do {
                long dateLong = cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATELONG));
                Date d = SatDateUtils.formatLongToDate(dateLong);
                vList = vMap.get(d);
                if(vList==null) {
                    vList = new ArrayList<VehicleObj>();
                }
                VehicleObj v = new VehicleObj();

                v.setDBId(cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_ID)));
                v.setFileName(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_FILENAME)));
                v.setGpsWeek(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_GPSWEEK)));
                v.setVehicleId(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_VID)));
                v.setDateStr(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATE)));
                v.setXcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_X)));
                v.setYcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Y)));
                v.setZcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Z)));
                v.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE)));
                v.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LONGITUDE)));
                v.setLatitude_hirvonenMoritz(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_HMM)));
                v.setLatitude_torge(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_TM)));
                v.setLatitude_astroAlmanac(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_AAM)));
                v.setLatitude_bowring(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_BWM)));
                v.setDebug(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DEBUG)));
                v.setDateLong(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATELONG)));
                v.setHealth(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_HEALTH)));
                v.setSystem(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_SYSTEM)));
                vList.add(v);
                vMap.put(d, vList);

                counter++;
                if(counter >= CtInc) {
                    p.incrementProgressBy(increment);
                    CtInc = CtInc + increment;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        GLog.logEnd(TAG, startTime1);
        //141106(141.106)
        //for 13320 DB rows: 11,580(11.58) |  10,789(10.789) | 11,556(11.556) | 87475(87.475) | 9075(9.075) | 9206(9.206) | 73870(73.87)

        return vMap;
    }

    public List<VehicleObj> getVehiclesList() throws ParseException{
        String selectQuery =
                "Select * FROM " + DBSQLiteHelper.TABLE_VEHICLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        List<VehicleObj> vList = new ArrayList();
        long startTime1 = System.nanoTime();
        if (cursor.moveToFirst()) {
            do {
                long dateLong = cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATELONG));
                Date d = SatDateUtils.formatLongToDate(dateLong);

                VehicleObj v = new VehicleObj();

                v.setDBId(cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_ID)));
                v.setFileName(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_FILENAME)));
                v.setGpsWeek(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_GPSWEEK)));
                v.setVehicleId(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_VID)));
                v.setDateStr(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATE)));
                v.setXcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_X)));
                v.setYcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Y)));
                v.setZcoordinate(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Z)));
                v.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE)));
                v.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LONGITUDE)));
                v.setLatitude_hirvonenMoritz(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_HMM)));
                v.setLatitude_torge(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_TM)));
                v.setLatitude_astroAlmanac(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_AAM)));
                v.setLatitude_bowring(cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_BWM)));
                v.setDebug(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DEBUG)));
                v.setDateLong(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATELONG)));
                v.setHealth(cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_HEALTH)));
                v.setSystem(cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_SYSTEM)));
                vList.add(v);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        GLog.logEnd(TAG, startTime1);
        //141106(141.106)
        //for 13320 DB rows: 11,580(11.58) |  10,789(10.789) | 11,556(11.556) | 87475(87.475) | 9075(9.075) | 9206(9.206) | 73870(73.87)

        return vList;
    }

    public String getVehiclesCSV() throws ParseException{
        String selectQuery =
                "Select * FROM " + DBSQLiteHelper.TABLE_VEHICLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        String s =  "COLUMN_VEH_ID,"+
                    "COLUMN_VEH_FILENAME," +
                    "COLUMN_VEH_GPSWEEK," +
                    "COLUMN_VEH_VID," +
                    "COLUMN_VEH_DATE," +
                    "COLUMN_VEH_X," +
                    "COLUMN_VEH_Y," +
                    "COLUMN_VEH_Z," +
                    "COLUMN_VEH_LATITUDE," +
                    "COLUMN_VEH_LONGITUDE," +
                    "COLUMN_VEH_LATITUDE_HMM," +
                    "COLUMN_VEH_LONGITUDE_HMM," +
                    "COLUMN_VEH_LATITUDE_TM," +
                    "COLUMN_VEH_LONGITUDE_TM," +
                    "COLUMN_VEH_LATITUDE_AAM," +
                    "COLUMN_VEH_LONGITUDE_AAM," +
                    "COLUMN_VEH_LATITUDE_BWM," +
                    "COLUMN_VEH_LONGITUDE_BWM," +
                    "COLUMN_VEH_DEBUG," +
                    "COLUMN_VEH_DATELONG," +
                    "COLUMN_VEH_HEALTH," +
                    "COLUMN_VEH_SYSTEM\r\n";

        if (cursor.moveToFirst()) {
            do {
                long dateLong = cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATELONG));
                Date d = SatDateUtils.formatLongToDate(dateLong);

                s = s + cursor.getLong(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_ID)) + "," +
                        cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_FILENAME)) + "," +
                        cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_GPSWEEK)) + "," +
                        cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_VID)) + "," +
                        cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DATE)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_X)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Y)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_Z)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LONGITUDE)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_HMM)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_TM)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_AAM)) + "," +
                        cursor.getDouble(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_LATITUDE_BWM)) + "," +
                        cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_DEBUG)) + "," +
                        dateLong + "," +
                        cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_HEALTH)) + "," +
                        cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_VEH_SYSTEM)) + "\r\n";
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection

        return s;
    }

    // Check if exists
    public boolean checkVehicleExists(String vIh, long datelong) {
        // Select All Query
        String selectQuery = "SELECT count(*) FROM " + TABLE_VEHICLE +
                " WHERE " + DBSQLiteHelper.COLUMN_VEH_VID + " = '" + vIh +
                "' AND " + DBSQLiteHelper.COLUMN_VEH_DATELONG + " = " + datelong;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        int vCt = cursor.getInt(0);
        boolean exists = vCt>0?true:false;
        cursor.close();
        db.close(); // Closing database connection

        // return results
        return exists;
    }

    // Deleting records by Date
    public void deleteVehicleListByDate(String dStr) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VEHICLE, DBSQLiteHelper.COLUMN_VEH_DATE + " = ?",
                new String[] { dStr });
        db.close();
    }

    // Deleting all records
    public void deleteVehicleList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VEHICLE, DBSQLiteHelper.COLUMN_VEH_ID + " != ?",
                new String[] { "-999" });
        db.close();
    }


    // Getting record count
    public int getVehiclesCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM "+TABLE_VEHICLE;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int vCt = mcursor.getInt(0);
        Log.d(TAG, "!!!!!!!!! vCt: "+vCt);
        mcursor.close();
        db.close(); // Closing database connection

        // return count
        return vCt;
    }

    // Getting latest date in DB
    public long getLatestDate() {
        String latestQuery = "SELECT " + COLUMN_VEH_DATELONG + " FROM " + TABLE_VEHICLE +
                " ORDER BY " + COLUMN_VEH_DATELONG + " DESC LIMIT 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(latestQuery, null);
        long dateLong = 0;

        if (cursor.moveToFirst()) {
            do {
                dateLong = cursor.getLong(cursor.getColumnIndex(COLUMN_VEH_DATELONG));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection

        // return count
        return dateLong;
    }

    // check if file has been read
    public Boolean hasFileBeenRead(String filename) {
        String count = "SELECT count(*) FROM "+TABLE_VEHICLE +
            " WHERE " + DBSQLiteHelper.COLUMN_VEH_FILENAME + " = '" + filename + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int vCt = mcursor.getInt(0);
        Boolean hasFileBeenRead = vCt>0?true:false;

        mcursor.close();
        db.close(); // Closing database connection

        // return count
        return hasFileBeenRead;
    }
}
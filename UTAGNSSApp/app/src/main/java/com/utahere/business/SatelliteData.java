package com.utahere.business;

import java.util.Calendar;
import java.util.List;

import com.utahere.data.DBSQLiteHelper;
import com.utahere.data.HTTPFileHelper;
import com.utahere.data.HTTPVehicleDataSource;
import com.utahere.math.xyz2geo;
import com.utahere.objects.VehicleObj;
import com.utahere.utils.SatDateUtils;

public class SatelliteData {
    private static final String TAG = SatelliteData.class.getSimpleName();

    public static VehicleObj getTestSatelliteData() {
		VehicleObj v = HTTPVehicleDataSource.getTestData();
		v = xyz2geo.xyz2geo_basic(v);
		return v;
	}
	
	public static List<VehicleObj> getTestSatelliteDataMap() {
		List<VehicleObj> vList = HTTPVehicleDataSource.getTestDataMap();
		return vList;
	}

    public static void getHttpSatelliteDataList(DBSQLiteHelper myDBHelper,
                                                int dateRange, String gpsURL, String gloURL) throws Exception {
        HTTPFileHelper.initiateDataDownload(myDBHelper, dateRange, gpsURL, gloURL);
    }

    public static String sendemail(DBSQLiteHelper myDBHelper) throws Exception {
        return myDBHelper.getVehiclesCSV();
    }

    public static int getGPSWeek(Calendar dCal) throws Exception {
        return SatDateUtils.calculateGPSWeek(dCal);
    }
}

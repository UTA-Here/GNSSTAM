package com.utahere.data;

import java.util.*;

import com.utahere.math.PositionCalculations;
import com.utahere.math.xyz2geo;
import com.utahere.objects.*;

public class HTTPVehicleDataSource {

    private static final String TAG = HTTPVehicleDataSource.class.getSimpleName();

    public static VehicleObj getTestData() {
		VehicleObj v = new VehicleObj("PG01", Calendar.getInstance(), 13544.150993, -21556.692811, 7229.530901);
		return v;
	}
	public static List<VehicleObj> getTestDataMap() {
		List<VehicleObj> vList = new ArrayList<VehicleObj>();
        vList.add(new VehicleObj("PG01", Calendar.getInstance(), 13544.150993, -21556.692811, 7229.530901));
        vList.add(new VehicleObj("PG02", Calendar.getInstance(), -15844.971361,   -917.385714, -20926.896916));
        vList.add(new VehicleObj("PG04", Calendar.getInstance(), 14062.143342, -20489.069024,   8636.249302));
        vList.add(new VehicleObj("PG05", Calendar.getInstance(), -26265.590183,   1412.655540,  -4367.269624));
        vList.add(new VehicleObj("PG06", Calendar.getInstance(), -9654.924754, -14439.578022, -20112.242371));
        vList.add(new VehicleObj("PG07", Calendar.getInstance(), 6046.458561, -25105.347022,   5426.296008));
        vList.add(new VehicleObj("PG08", Calendar.getInstance(), 14755.963328, -20986.761767,  -5127.394924));
        vList.add(new VehicleObj("PG09", Calendar.getInstance(), -2516.143791, -20832.653482, -16271.242364));
        vList.add(new VehicleObj("PG10", Calendar.getInstance(), -16947.799855, -10602.598529, -18050.126895));

        for(int i=0; i<vList.size(); i++) {
            xyz2geo.xyz2geo_basic(vList.get(i));
            PositionCalculations.xyz2enu(vList.get(i));
            PositionCalculations.SatelliteElevationAndAzimuth(vList.get(i));
        }

        return vList;
	}
}

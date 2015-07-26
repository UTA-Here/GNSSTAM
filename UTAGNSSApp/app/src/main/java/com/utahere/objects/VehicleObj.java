package com.utahere.objects;

import com.utahere.math.xyz2geo;
import com.utahere.utils.SatDateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import Jama.Matrix;

public class VehicleObj {
	public final static int HEALTH_GOOD = 1;
	public final static int HEALTH_UNKNOWN = 0;
	public final static int HEALTH_BAD = -1;

    public Long dbId;
    public String system;
	public String vehicleId;
    public String fileName;
    public String debug;
    public int lineNum;
    public int gpsWeek;

	//cartesian coordinates:
	public double xcoordinate;
	public double ycoordinate;
	public double zcoordinate;
	
	//geographical coordinates:
	public double latitude;
	public double longitude;
	public double altitude;
    public double latitude_basic;
    public double altitude_basic;
    public double latitude_hirvonenMoritz;
    public double altitude_hirvonenMoritz;
    public double latitude_torge;
    public double altitude_torge;
    public double latitude_astroAlmanac;
    public double altitude_astroAlmanac;
    public double latitude_bowring;
    public double altitude_bowring;
    public int numOfIterations_hirvonenMoritz;
    public int numOfIterations_torge;
    public int numOfIterations_astroAlmanac;
    public int numOfIterations_bowring;

    //user geographical coordinates:
    public double userlatitude;
    public double userlongitude;
    public double useraltitude;

    //east-north-up coordinates:
    public Matrix enu;
	public double east;
	public double north;
	public double up;
	
	public double elevation;
	public double azimuth;
	
	public int health;
	public String why;

    public String year;
    public String month;
    public String day;
    public String hour;
    public String min;

    public Calendar date;
    public String dateStr;
    public long dateLong;

    public VehicleObj() {
		super();
	}

    public VehicleObj(String vehicleId, Calendar ddate,
                      double xcoordinate, double ycoordinate, double zcoordinate) {
		super();
        this.date = ddate;
        this.dateStr = SatDateUtils.formatDateToStr(ddate.getTime());
        this.vehicleId = vehicleId;
		this.xcoordinate = xcoordinate;
		this.ycoordinate = ycoordinate;
		this.zcoordinate = zcoordinate;
        xyz2geo.xyz2geo_basic(this);
    }

    public Long getDBId() {
        return dbId;
    }
    public void setDBId(Long dbId) {
        this.dbId = dbId;
    }
    public String getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getDebug() {
        return debug;
    }
    public void setDebug(String debug) {
        this.debug = debug;
    }

    public int getLineNum() { return lineNum; }
    public void setLineNum(int lineNum) { this.lineNum = lineNum; }
    public int getGpsWeek() { return gpsWeek; }
    public void setGpsWeek(int gpsWeek) { this.gpsWeek = gpsWeek; }
	public double getXcoordinate() {
		return xcoordinate;
	}
	public void setXcoordinate(double xcoordinate) {
		this.xcoordinate = xcoordinate;
	}
	public double getYcoordinate() {
		return ycoordinate;
	}
	public void setYcoordinate(double ycoordinate) {
		this.ycoordinate = ycoordinate;
	}
	public double getZcoordinate() {
		return zcoordinate;
	}
	public void setZcoordinate(double zcoordinate) {
		this.zcoordinate = zcoordinate;
	}
	public double getUserLatitude() {
		return userlatitude;
	}
	public void setUserLatitude(double userlatitude) {
		this.userlatitude = userlatitude;
	}
	public double getUserLongitude() {
		return userlongitude;
	}
	public void setUserLongitude(double userlongitude) {
		this.userlongitude = userlongitude;
	}
	public double getUserAltitude() {
		return useraltitude;
	}
	public void setUserAltitude(double useraltitude) {
		this.useraltitude = useraltitude;
	}
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getAltitude() {
        return altitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAltitude_basic() {
        return altitude_basic;
    }

    public void setAltitude_basic(double altitude_basic) {
        this.altitude_basic = altitude_basic;
    }

    public double getLatitude_basic() {
        return latitude_basic;
    }

    public void setLatitude_basic(double latitude_basic) {
        this.latitude_basic = latitude_basic;
    }

    public double getLatitude_hirvonenMoritz() {
        return latitude_hirvonenMoritz;
    }

    public void setLatitude_hirvonenMoritz(double latitude_hirvonenMoritz) {
        this.latitude_hirvonenMoritz = latitude_hirvonenMoritz;
    }

    public double getAltitude_hirvonenMoritz() {
        return altitude_hirvonenMoritz;
    }

    public void setAltitude_hirvonenMoritz(double altitude_hirvonenMoritz) {
        this.altitude_hirvonenMoritz = altitude_hirvonenMoritz;
    }

    public double getLatitude_torge() {
        return latitude_torge;
    }

    public void setLatitude_torge(double latitude_torge) {
        this.latitude_torge = latitude_torge;
    }

    public double getAltitude_torge() {
        return altitude_torge;
    }

    public void setAltitude_torge(double altitude_torge) {
        this.altitude_torge = altitude_torge;
    }

    public double getLatitude_astroAlmanac() {
        return latitude_astroAlmanac;
    }

    public void setLatitude_astroAlmanac(double latitude_astroAlmanac) {
        this.latitude_astroAlmanac = latitude_astroAlmanac;
    }

    public double getAltitude_astroAlmanac() {
        return altitude_astroAlmanac;
    }

    public void setAltitude_astroAlmanac(double altitude_astroAlmanac) {
        this.altitude_astroAlmanac = altitude_astroAlmanac;
    }

    public double getLatitude_bowring() {
        return latitude_bowring;
    }

    public void setLatitude_bowring(double latitude_bowring) {
        this.latitude_bowring = latitude_bowring;
    }

    public double getAltitude_bowring() {
        return altitude_bowring;
    }

    public void setAltitude_bowring(double altitude_bowring) {
        this.altitude_bowring = altitude_bowring;
    }

    public int getNumOfIterations_hirvonenMoritz() {
        return numOfIterations_hirvonenMoritz;
    }

    public void setNumOfIterations_hirvonenMoritz(int numOfIterations_hirvonenMoritz) {
        this.numOfIterations_hirvonenMoritz = numOfIterations_hirvonenMoritz;
    }

    public int getNumOfIterations_torge() {
        return numOfIterations_torge;
    }

    public void setNumOfIterations_torge(int numOfIterations_torge) {
        this.numOfIterations_torge = numOfIterations_torge;
    }

    public int getNumOfIterations_astroAlmanac() {
        return numOfIterations_astroAlmanac;
    }

    public void setNumOfIterations_astroAlmanac(int numOfIterations_astroAlmanac) {
        this.numOfIterations_astroAlmanac = numOfIterations_astroAlmanac;
    }

    public int getNumOfIterations_bowring() {
        return numOfIterations_bowring;
    }

    public void setNumOfIterations_bowring(int numOfIterations_bowring) {
        this.numOfIterations_bowring = numOfIterations_bowring;
    }

    public Matrix getENU() {
        return enu;
    }
    public void setENU(Matrix enu) { this.enu = enu; }
	public double getEast() {
		return east;
	}
	public void setEast(double east) {
		this.east = east;
	}
	public double getNorth() {
		return north;
	}
	public void setNorth(double north) {
		this.north = north;
	}
	public double getUp() {
		return up;
	}
	public void setUp(double up) {
		this.up = up;
	}
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	public double getAzimuth() {
		return azimuth;
	}
	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}
	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}
	public String getWhy() {
		return why;
	}
	public void setWhy(String why) {
		this.why = why;
	}
    public long getDateLong() { return dateLong; }
    public void setDateLong(long dateLong) { this.dateLong = dateLong; }
    public Calendar getDate() { return date; }
    public void setDate(Calendar date) { this.date = date; }
    public String getDateStr() { return dateStr; }
    public void setDateStr(String dateStr) throws ParseException {
        this.dateStr = dateStr;
        //Format and set Calendar date
        //Source: http://stackoverflow.com/questions/5301226/convert-string-to-calendar-object-in-java
        Calendar cal = Calendar.getInstance();
        //2015-02-27 18:00:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        cal.setTime(sdf.parse(dateStr));
        this.date = cal;
    }
}

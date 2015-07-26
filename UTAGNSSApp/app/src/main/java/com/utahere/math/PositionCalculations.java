package com.utahere.math;

import com.utahere.objects.VehicleObj;

import Jama.*;

public class PositionCalculations {
    private static final String TAG = PositionCalculations.class.getSimpleName();

    public static int R_eq = 6378137; //Equatorial cross-section radius in m / semimajor axis (mean equatorial radius) [m]
    public static double R_pol = 6356752.3142; //Polar cross-section radius in m / semiminor axis [m]
    public static double ep = 0.0820944379496; //second eccentricity

    //Convert lat/long to xyz
    //Inputs:
    //	latitude
    //	longitude
    //TESTED!
    public static VehicleObj geo2xyz(VehicleObj v) {
        double lat = DegtoRad(v.latitude);
        double lon = DegtoRad(v.longitude);
        double alt = v.altitude; //Do we need to convert this?

        // eccentricity
        double e = Math.sqrt(1 - (Math.pow(R_pol, 2) / Math.pow(R_eq, 2)));
        // CONVERSION
        // NOTE: ASSUMES ANGLES BEEN GIVEN IN RADIANS

        double e_sq = Math.pow(e, 2);
        double lat_tan_sq = Math.pow(Math.tan(lat), 2);
        double lat_sin_sq = Math.pow(Math.sin(lat), 2);

        v.xcoordinate = R_eq * Math.cos(lon) / Math.sqrt(1 + (1 - e_sq) * lat_tan_sq) + alt * Math.cos(lon) * Math.cos(lat);
        v.ycoordinate = R_eq * Math.sin(lon) / Math.sqrt(1 + (1 - e_sq) * lat_tan_sq) + alt * Math.sin(lon) * Math.cos(lat);
        v.zcoordinate = R_eq * (1 - e_sq) * Math.sin(lat) / Math.sqrt(1 - e_sq * lat_sin_sq) + alt * Math.sin(lat);

        return v;
    }

    //Convert xyz to lat/long
    //Convert to radians first:
    //2pi radians = 360 degrees
    //Inputs:
    //	xcoordinate
    //	ycoordinate
    //	zcoordinate
    //TESTED!
    public static VehicleObj xyz2geo_obsolete(VehicleObj v) {
        // semimajor axis (mean equatorial radius) [m]
        //R_eq --> a = 6378137;
        //R_pol --> b = 6356752.3142;
        // eccentricity
        double e = Math.sqrt(1 - (Math.pow(R_pol, 2) / Math.pow(R_eq, 2)));
        double e_sq = Math.pow(e, 2);
        double e_qt = Math.pow(e, 4);
        double z_sq = Math.pow(v.zcoordinate, 2);

        //Evaluation of Geodetic height and latitude
        double r = Math.sqrt(Math.pow(v.xcoordinate, 2) + Math.pow(v.ycoordinate, 2));
        double r_sq = Math.pow(r, 2);
        double E2 = Math.pow(R_eq, 2) - Math.pow(R_pol, 2);
        double f = 54 * Math.pow(R_pol, 2) * z_sq;
        double G = Math.pow(r, 2) + ((1 - e_sq) * z_sq) - (e_sq * E2);
        double c = (e_qt * f * r_sq) / Math.pow(G, 3);
        double s = Math.pow((1 + c + Math.sqrt(Math.pow(c, 2) + 2 * c)), (1 / 3));
        double P = f / (3 * Math.pow((s + 1 / s + 1), 2) * Math.pow(G, 2));
        double Q = Math.sqrt(1 + 2 * e_qt * P);
        double r0 = -P * e_sq * r / (1 + Q) + Math.sqrt(0.5 * Math.pow(R_eq, 2) * (1 + 1 / Q) - P * (1 - e_sq) * z_sq / (Q * (1 + Q)) - 0.5 * P * r_sq);
        double U = Math.sqrt(Math.pow((r - e_sq * r0), 2) + z_sq);
        double V = Math.sqrt(Math.pow((r - e_sq * r0), 2) + (1 - e_sq) * z_sq);
        double z0 = (Math.pow(R_pol, 2) * v.zcoordinate) / (R_eq * V);

        //https://courseware.e-education.psu.edu/courses/bootcamp/lo01/cg.html
        v.altitude = U * (1 - Math.pow(R_pol, 2) / (R_eq * V));
        v.latitude = Math.atan2((v.zcoordinate + Math.pow(ep, 2) * z0), r);

        if (v.xcoordinate >= 0) {
            v.longitude = Math.atan(v.ycoordinate / v.xcoordinate);
        } else if (v.xcoordinate < 0 && v.ycoordinate >= 0) {
            v.longitude = Math.atan(v.ycoordinate / v.xcoordinate) + Math.PI;
        } else {
            v.longitude = Math.atan(v.ycoordinate / v.xcoordinate) - Math.PI;
        }

        //wrap longitude: http://research.microsoft.com/en-us/projects/wraplatitudelongitude/
        v.longitude = v.longitude + 2 * Math.PI * ((v.longitude < 0) ? 1 : 0) - 2 * Math.PI * ((v.longitude > 2 * Math.PI) ? 1 : 0);

        /*

        C:\temp>java TestWrapLongitude 100
        --- lon: 93.7168146928204

        C:\temp>java TestWrapLongitude 90
        --- lon: 83.7168146928204

        C:\temp>java TestWrapLongitude 95
        --- lon: 88.7168146928204
        I thought the North Pole is +90 deg, so shouldn't +95 deg be wrapped to +85 deg?

        C:\temp>java TestWrapLongitude 80
        --- lon: 73.7168146928204

        C:\temp>java TestWrapLongitude 75
        --- lon: 68.7168146928204

        C:\temp>java TestWrapLongitude -90
        --- lon: -83.7168146928204

        CONVERT TO RADIANS:
        C:\temp>java TestWrapLongitude 95
        --- 1.6580627893946132 : 95.0

        C:\temp>java TestWrapLongitude 185
        --- 3.2288591161895095 : 185.0

        C:\temp>java TestWrapLongitude 700
        --- 12.217304763960307 : 340.0

        C:\temp>java TestWrapLongitude 100
        --- 1.7453292519943295 : 100.0

        C:\temp>java TestWrapLongitude 200
        --- 3.490658503988659 : 200.0

        C:\temp>java TestWrapLongitude 360
        --- 6.283185307179586 : 360.0

        C:\temp>java TestWrapLongitude 720
        --- 12.566370614359172 : 360.0

        C:\temp>java TestWrapLongitude 0
        --- 0.0 : 0.0

        C:\temp>java TestWrapLongitude -720
        --- -12.566370614359172 : -360.0

        C:\temp>java TestWrapLongitude 480
        --- 8.377580409572781 : 119.99999999999997

         */
        //Convert to degrees
        v.latitude = RadtoDeg(v.latitude);
        v.longitude = RadtoDeg(v.longitude);

        return v;
    }

    //Alternate code
    //Source: http://stackoverflow.com/questions/18253546/ecef-to-lla-lat-lon-alt-in-java
    //TESTED!
    public static VehicleObj xyz2geo2(VehicleObj v) {
        double a = 6378137; // radius
        double e = 8.1819190842622e-2;  // eccentricity
        double asq = Math.pow(a, 2);
        double esq = Math.pow(e, 2);

        double x = v.xcoordinate;
        double y = v.ycoordinate;
        double z = v.zcoordinate;

        double b = Math.sqrt(asq * (1 - esq));
        double bsq = Math.pow(b, 2);
        double ep = Math.sqrt((asq - bsq) / bsq);
        double p = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double th = Math.atan2(a * z, b * p);

        double lon = Math.atan2(y, x);
        double lat = Math.atan2((z + Math.pow(ep, 2) * b * Math.pow(Math.sin(th), 3)), (p - esq * a * Math.pow(Math.cos(th), 3)));
        double N = a / (Math.sqrt(1 - esq * Math.pow(Math.sin(lat), 2)));
        double alt = p / Math.cos(lat) - N;

        // mod lat to 0-2pi
        lon = lon % (2 * Math.PI);
        v.latitude = RadtoDeg(lat);
        v.longitude = RadtoDeg(lon);
        v.altitude = alt;

        // correction for altitude near poles
        /*
	    if( v.xcoordinate >= 0){
	    	v.longitude = Math.atan(v.ycoordinate/v.xcoordinate);
	    } else if(v.xcoordinate < 0 && v.ycoordinate >= 0 ){
	    	v.longitude = Math.atan(v.ycoordinate/v.xcoordinate) + Math.PI;
	    } else {
	    	v.longitude = Math.atan(v.ycoordinate/v.xcoordinate) - Math.PI;
	    }
	    */

        return v;
    }

    //xyz2enu <- function(llh,Dxyz){
    //Inputs:
    //	xcoordinate
    //	ycoordinate
    //TESTED!
    public static VehicleObj xyz2enu(VehicleObj v, double userX, double userY, double userZ) {
        // First, we need to get latitude and longitude
        xyz2geo.xyz2geo_basic(v);

        // FORM ROTATION MATRIX
        double sinlat = Math.sin(v.latitude);
        double coslat = Math.cos(v.latitude);
        double sinlong = Math.sin(v.longitude);
        double coslong = Math.cos(v.longitude);

        //new double[3][3];
        double[][] ROTA = {{-sinlong, coslong, 0},
                {-sinlat * coslong, -sinlat * sinlong, coslat},
                {coslat * coslong, coslat * sinlong, sinlat}};

        //ROTATE TARGET TO LOCAL ENU
        //enu = ROTA %*% Dxyz;
        Matrix mROTA = new Matrix(ROTA);
        Matrix mDXYZ = new Matrix(new double[][]{{userX},
                {userY},
                {userZ}});
        Matrix mENU = mROTA.times(mDXYZ);

        v.enu = mENU;
        v.east = mENU.get(0, 0);
        v.north = mENU.get(1, 0);
        v.up = mENU.get(2, 0);

        return v;
    }

    //xyz2enu <- function(llh,Dxyz){
    //Inputs:
    //	xcoordinate
    //	ycoordinate
    //TESTED!
    public static VehicleObj xyz2enu(VehicleObj v) {
        // First, we need to get latitude and longitude
        xyz2geo.xyz2geo_basic(v);

        // FORM ROTATION MATRIX
        double sinlat = Math.sin(v.latitude);
        double coslat = Math.cos(v.latitude);
        double sinlong = Math.sin(v.longitude);
        double coslong = Math.cos(v.longitude);

        //new double[3][3];
        double[][] ROTA = {{-sinlong, coslong, 0},
                {-sinlat * coslong, -sinlat * sinlong, coslat},
                {coslat * coslong, coslat * sinlong, sinlat}};

        //ROTATE TARGET TO LOCAL ENU
        //enu = ROTA %*% Dxyz;
        Matrix mROTA = new Matrix(ROTA);
        Matrix mDXYZ = new Matrix(new double[][]{{v.xcoordinate},
                {v.ycoordinate},
                {v.zcoordinate}});
        Matrix mENU = mROTA.times(mDXYZ);

        v.enu = mENU;
        v.east = mENU.get(0, 0);
        v.north = mENU.get(1, 0);
        v.up = mENU.get(2, 0);

        return v;
    }

    //enu2xyz <- function (llh,enu){
    //Inputs:
    //	xcoordinate
    //	ycoordinate
    //TESTED!
    public static VehicleObj enu2xyz(VehicleObj v) {
        //FORM ROTATION MATRIX
        double sinlat = Math.sin(v.latitude);
        double coslat = Math.cos(v.latitude);
        double sinlong = Math.sin(v.longitude);
        double coslong = Math.cos(v.longitude);

        double[][] ROTA = {{-sinlong, coslong, 0},
                {-sinlat * coslong, -sinlat * sinlong, coslat},
                {coslat * coslong, coslat * sinlong, sinlat}};

        //ROTATE TARGET TO GLOBAL XYZ
        Matrix mROTA = new Matrix(ROTA);
        Matrix mENU = new Matrix(new double[][]{{v.east},
                {v.north},
                {v.up}});
        Matrix mXYZ = mROTA.solve(mENU); //inv(ROTA)*enu
                                        // http://en.wikipedia.org/wiki/Geodetic_datum#From_ENU_to_ECEF
                                        // + user's xyz-location
        v.xcoordinate = mXYZ.get(0, 0);
        v.ycoordinate = mXYZ.get(1, 0);
        v.zcoordinate = mXYZ.get(2, 0);
        return v;
    }

    //Inputs:
    //	time
    //	time1
    //	time2
    //	pos1
    //	pos2
    public static VehicleObj InterpolateSatPos(VehicleObj v) {
        //function(time,pos1,time1,pos2,time2){

        //pos = pos1 + ((time-time1)/(time2-time1))*(pos2-pos1)

        return v;
    }

    //Inputs:
    //	east
    //	north
    //	up
    //TODO: elevation and azimuth has to be from user's location!
    public static VehicleObj SatelliteElevationAndAzimuth(VehicleObj v) {
        //function(satpos,userpos){

        //satellite - user position difference in xyz coordinates
        //Dxyz=satpos-userpos;

        //user position in geodetic coordinates
        //llh = xyz2geo(userpos)

        //compute satellite position in user-centered enu-coordinates
        //sat_enu = xyz2enu(llh,Dxyz)

        //elevation  = arctan(up/sqrt(east^2+west^2)) [rad]
        v.elevation = Math.atan(v.up / Math.sqrt(Math.pow(v.east, 2) + Math.pow(v.north, 2)));

        //azimuth = atan2(east,north)
        v.azimuth = Math.atan2(v.east, v.north);

        return v;

    }

    //TESTED!
    public static double DegtoRad(double angdeg) {
        return Math.toRadians(angdeg);
    }

    //TESTED!
    public static double RadtoDeg(double angrad) {
        return Math.toDegrees(angrad);
    }
}

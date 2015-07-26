package com.utahere.math;

import com.utahere.objects.VehicleObj;

/**
 * Created by Tsailing on 6/3/2015.
 * Source: http://geostarslib.sourceforge.net/index.html
 *
 * This class contains various methods to convert xyz to lat/long
 */
public class xyz2geo {
    private static final String TAG = xyz2geo.class.getSimpleName();

    public static double a = 6378.137; //Equatorial cross-section radius in m / semimajor axis (mean equatorial radius) [m]
    public static double b = 6356752.3142; //Polar cross-section radius in m / semiminor axis [m]
    public static double precision = 0.00001;

    //Basic Method
    public static VehicleObj xyz2geo_basic(VehicleObj v) {
		a = 6378.137;

        // eccentricity
        double f = 1/298.257224;
        // double e2 = f * (2-f);
        double e2 = 0.00669437999013;

        double E = v.xcoordinate;
        double F = v.ycoordinate;
        double G = v.zcoordinate;
        double p = Math.sqrt(Math.pow(E,2) + Math.pow(F,2));

        //first approximation:
        double latitude = Math.atan((G/p) * (1/(1-e2)));
        double N = a / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(latitude)), 2))));
        double altitude = (p / (Math.cos(latitude))) - N;


        v.latitude = PositionCalculations.RadtoDeg(latitude);
        v.longitude = calculateLogitude(v);
        v.altitude = altitude;

        return v;
    }

    //Basic Method - without longitude calculation for benchmarking
    public static VehicleObj xyz2geo_basic2(VehicleObj v) {
        a = 6378.137;

        // eccentricity
        double f = 1/298.257224;
        // double e2 = f * (2-f);
        double e2 = 0.00669437999013;

        double E = v.xcoordinate;
        double F = v.ycoordinate;
        double G = v.zcoordinate;
        double p = Math.sqrt(Math.pow(E,2) + Math.pow(F,2));

        //first approximation:
        double latitude = Math.atan((G/p) * (1/(1-e2)));
        double N = a / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(latitude)), 2))));
        double altitude = (p / (Math.cos(latitude))) - N;


        v.latitude_basic = PositionCalculations.RadtoDeg(latitude);
        v.altitude_basic = altitude;

        return v;
    }

        //Hirvonen and Moritz Method
    public static VehicleObj xyz2geo_hirvonenMoritz(VehicleObj v) {
		a = 6378.137;

        // eccentricity
        double f = 1/298.257224;
        //double e2 = f * (2-f);
        double e2 = 0.00669437999013;

        double E = v.xcoordinate;
        double F = v.ycoordinate;
        double G = v.zcoordinate;

        double p = Math.sqrt(Math.pow(E,2) + Math.pow(F,2));

        //first approximation:
        double lat1 = Math.atan((G/p) * (1/(1-e2)));
        double N = a / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(lat1)), 2))));

        //iterate lat1 until change in lat is insignificant:
        double latitude = Math.atan((G / p) * (1 + ((e2 * N * Math.sin(lat1)) / G)));
        int numOfIts = 0;
        while(Math.abs(Math.abs(PositionCalculations.RadtoDeg(lat1)) - Math.abs(PositionCalculations.RadtoDeg(latitude))) > precision) {
            lat1 = latitude;
            double newN = a / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(lat1)), 2))));
            if(!Double.isNaN(newN)) {
				N = newN;
				latitude = Math.atan2(G / p, (1 + (e2 * N * Math.sin(lat1) / G)));
			} else {
				lat1 = latitude;
			}
            numOfIts++;
        }//end iterate

        double altitude = (p / (Math.cos(lat1))) - N;

        v.latitude_hirvonenMoritz = PositionCalculations.RadtoDeg(latitude);
        v.altitude_hirvonenMoritz = altitude;
        v.numOfIterations_hirvonenMoritz = numOfIts;

        return v;
    }

    //Torge Method / Heiskanen and Moritz Method
    public static VehicleObj xyz2geo_torge(VehicleObj v) {
		a = 6378.137;

        // eccentricity
        double f = 1/298.257224;
        //double e2 = f * (2-f);
        double e2 = 0.00669437999013;

        double E = v.xcoordinate;
        double F = v.ycoordinate;
        double G = v.zcoordinate;

        double p = Math.sqrt(Math.pow(E,2) + Math.pow(F,2));

        //first approximation:
        double lat1 = Math.atan((G/p) * (1/(1-e2)));
        double N = a / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(lat1)), 2))));
        double altitude = (p / (Math.cos(lat1))) - N;

        //iterate lat1 until change in lat is insignificant:
        double latitude = Math.atan((G / p) * (1 / (1-(e2 * N/(N+altitude)))));
        int numOfIts = 0;
        while(Math.abs(Math.abs(PositionCalculations.RadtoDeg(lat1)) - Math.abs(PositionCalculations.RadtoDeg(latitude))) > precision) {
            lat1 = latitude;
            double newN = a / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(lat1)), 2))));
            altitude = (p / (Math.cos(lat1))) - N;
            if(!Double.isNaN(newN)) {
				N = newN;
				latitude = Math.atan((G / p) * (1 / (1-(e2 * N/(N+altitude)))));
			} else {
				lat1 = latitude;
			}
            numOfIts++;
        }//end iterate


        v.latitude_torge = PositionCalculations.RadtoDeg(latitude);
        v.altitude_torge = altitude;
        v.numOfIterations_torge = numOfIts;

        return v;
    }

    //Astronomical Almanac 2002 Method
    public static VehicleObj xyz2geo_astroAlmanac(VehicleObj v) {
        a = 6378.137;

        // eccentricity
        double f = 1/298.257224;
        double e2 = 0.00669437999013;

        double E = v.xcoordinate;
        double F = v.ycoordinate;
        double G = v.zcoordinate;

        double p = Math.sqrt(Math.pow(E,2) + Math.pow(F,2));

        //prepare initial estimate:
        double lat1 = Math.atan(G/p);
        double N = 1 / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(lat1)), 2))));
        double latitude = Math.atan((G + (a * N * e2 * Math.sin(lat1))) / p);

        //Iterate lat1 until change in lat is insignificant:
        int numOfIts = 0;
        while(Math.abs(Math.abs(PositionCalculations.RadtoDeg(lat1)) - Math.abs(PositionCalculations.RadtoDeg(latitude))) > precision) {
            lat1 = latitude;
            double newN = 1 / (Math.sqrt(1 - (e2 * Math.pow((Math.sin(lat1)), 2))));
            if(!Double.isNaN(newN)) {
                N = newN;
                latitude = Math.atan((G + (a * N * e2 * Math.sin(lat1))) / p);
            } else {
                lat1 = latitude;
            }
            numOfIts++;
        }//end iterate

        double altitude = (p / (Math.cos(lat1))) - (a*N);

        v.latitude_astroAlmanac = PositionCalculations.RadtoDeg(latitude);
        v.altitude_astroAlmanac = altitude;
        v.numOfIterations_astroAlmanac = numOfIts;

        return v;
    }

    //Bowring Method
    //Source: http://www.movable-type.co.uk/scripts/latlong-convert-coords.html
    public static VehicleObj xyz2geo_bowring(VehicleObj v) {
        a = 6378.137;
        b = 6356.7523142;
        double x = v.xcoordinate;
        double y = v.ycoordinate;
        double z = v.zcoordinate;

        double E1 = (Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(a, 2); // 1st eccentricity squared
        double E2 = (Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(b, 2); // 2nd eccentricity squared

        double p = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); // distance from minor axis
        double R = Math.sqrt(Math.pow(p,2) + Math.pow(z,2)); // polar radius

        // parametric latitude (Bowring eqn 17, replacing tanb = zÂ·a / pÂ·b)
        double tanb = (b*z)/(a*p) * (1+E2*b/R);
        double sinb = tanb / Math.sqrt(1+Math.pow(tanb,2));
        double cosb = (tanb==0)?0:(sinb / tanb);

        // geodetic latitude (Bowring eqn 18)
        double latitude = Math.atan2(z + E2*b*Math.pow(sinb,3), p - E1*a*Math.pow(cosb,3));

        // height above ellipsoid (Bowring eqn 7) [not currently used]
        double sinlat = Math.sin(latitude);
        double coslat = Math.cos(latitude);
        double norm = a*Math.sqrt(1-E1*Math.pow(sinlat,2)); // length of the normal terminated by the minor axis
        double altitude = (p*coslat) + (z*sinlat) - (Math.pow(a,2)/norm);

        v.latitude_bowring = PositionCalculations.RadtoDeg(latitude);
        v.altitude_bowring = altitude;

        return v;
    }


    //Bowring Method
    //Source: http://www.movable-type.co.uk/scripts/latlong-convert-coords.html
    public static VehicleObj xyz2geo_bowring_alternative(VehicleObj v) {
		a = 6378.137;
		b = 6356.7523142;
        double x = v.xcoordinate;
        double y = v.ycoordinate;
        double z = v.zcoordinate;

        double E1 = (Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(a, 2); // 1st eccentricity squared
        double E2 = (Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(b, 2); // 2nd eccentricity squared

        double p = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); // distance from minor axis
        double R = Math.sqrt(Math.pow(p,2) + Math.pow(z,2)); // polar radius

        // initial estimate of parametric latitude (Bowring eqn 17, replacing tanb = z·a / p·b)
        double beta = Math.atan((b/a) * (1+(E2*b/R)));
        double tanlat = (z+(E2*b*Math.pow(Math.sin(beta),3)))/(p-(E1*a*Math.pow(Math.cos(beta),3)));
        double beta1 = Math.atan((b/a) * tanlat);

        //Iterate lat1 until change in lat is insignificant:
        int numOfIts = 0;
        while(Math.abs(Math.abs(PositionCalculations.RadtoDeg(beta1)) - Math.abs(PositionCalculations.RadtoDeg(beta))) > precision) {
            beta = Math.atan((b/a) * (1+(E2*b/R)));
            tanlat = (z+(E2*b*Math.pow(Math.sin(beta),3)))/(p-(E1*a*Math.pow(Math.cos(beta),3)));
            beta1 = Math.atan((b/a) * tanlat);

            beta = beta1;
            numOfIts++;
        }//end iterate

        double latitude = Math.atan(tanlat);
        // height above ellipsoid (Bowring eqn 7) [not currently used]
        double sinlat = Math.sin(latitude);
        double coslat = Math.cos(latitude);
        double norm = a*Math.sqrt(1-E1*Math.pow(sinlat,2)); // length of the normal terminated by the minor axis
        double altitude = (p*coslat) + (z*sinlat) - (Math.pow(a,2)/norm);

        v.latitude_bowring = PositionCalculations.RadtoDeg(latitude);
        v.altitude_bowring = altitude;

        return v;
    }


    // longitude
    public static double calculateLogitude(VehicleObj v) {
        double x = v.xcoordinate;
        double y = v.ycoordinate;
        double z = v.zcoordinate;
        double longitude = 0;
        if (x >= 0) {
            longitude = Math.atan(y / x);
        } else if (x < 0 && y >= 0) {
            longitude = Math.atan(y / x) + Math.PI;
        } else {
            longitude = Math.atan(y / x) - Math.PI;
        }
        //wrap longitude: http://research.microsoft.com/en-us/projects/wraplatitudelongitude/
        longitude = longitude + 2 * Math.PI * ((longitude < 0) ? 1 : 0) - 2 * Math.PI * ((longitude > 2 * Math.PI) ? 1 : 0);
        longitude = PositionCalculations.RadtoDeg(longitude);
        return longitude;
    }
}

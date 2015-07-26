package com.utahere.data;

import android.os.Environment;
import android.util.Log;

import com.googlecode.compress.LZCInputStream;
import com.utahere.Exception.ExecutionException;
import com.utahere.objects.VehicleObj;
import com.utahere.utils.SatDateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by sailing on 3/19/2015.
 */
public class HTTPFileHelper {

    private static final String TAG = HTTPFileHelper.class.getSimpleName();
    //public static final String serverURL = "https://igscb.jpl.nasa.gov/igscb/product/";
    public static String gps_serverURL = "https://igscb.jpl.nasa.gov/igscb/product/";
    public static String glo_serverURL = "https://igscb.jpl.nasa.gov/igscb/glonass/products/";

    public static void initiateDataDownload(DBSQLiteHelper myDBHelper,
                                            int numOfGPSWeeks, String gpsURL, String gloURL) throws Exception {
        //Get GPS week
        //Calendar today = new GregorianCalendar();
        //String weekNum = getGPSWeekFromDate(today);
        myDBHelper.DropDB();
        gps_serverURL = (gpsURL==null||gpsURL.trim()=="")?gps_serverURL:gpsURL;
        glo_serverURL = (gloURL==null||gloURL.trim()=="")?glo_serverURL:gloURL;
        int weekNum = getGPSWeekFromServer(gps_serverURL);
        if(weekNum<=0) {
            Calendar today = new GregorianCalendar();
            weekNum = getGPSWeekFromDate(today);
        }

        try {
            getIgscbData("GLONASS", glo_serverURL, weekNum, myDBHelper, numOfGPSWeeks);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        try {
            getIgscbData("GPS", gps_serverURL, weekNum, myDBHelper, numOfGPSWeeks);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void getIgscbData(String system, String serverURL, int weekNum, DBSQLiteHelper myDBHelper, int numOfGPSWeeks) throws Exception {
        //String startingNumStr = (startingNumInt<1000?"0":"") + startingNumInt;
        Log.d(TAG, "0000000000000000000000 system: "+system + ": " + serverURL);
        int endGPSWeeks = weekNum - numOfGPSWeeks;
        while(weekNum > endGPSWeeks) {
            String targetURL = serverURL + weekNum + "/";
            URLReader(system, myDBHelper, weekNum, targetURL);
            weekNum = weekNum -1;
        }
    }

    //Get GPS week by current date/time
    public static int getGPSWeekFromDate(Calendar today) throws Exception {
        return SatDateUtils.calculateGPSWeek(today);
    }

    //retrieve latest GPS week from server data
    public static int getGPSWeekFromServer(String serverURL) throws Exception {
        long startTime = System.nanoTime();
/*
     *  fix for
     *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
     *       sun.security.validator.ValidatorException:
     *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
     *               unable to find valid certification path to requested target
     *   Source: http://www.rgagnon.com/javadetails/java-fix-certificate-problem-in-HTTPS.html
     */
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    /*
     * end of the fix
     */

        URL oracle = new URL(serverURL);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        String weekNum = "0";
        while ((inputLine = in.readLine()) != null) {
            if(inputLine.length() > 50 && inputLine.indexOf("href")>13 && inputLine.indexOf("/\"><img src=\"/icons/folder.gif\" alt=\"[DIR]\" width=\"16\" height=\"16\" /></a></td><td><a href=\"")>17) {
                String pattern = "^\\d+$";
                weekNum = inputLine.substring(inputLine.indexOf("href")+6,inputLine.indexOf("/\"><img src=\""));
                if(weekNum.matches(pattern)) {
                    break;
                }
            }
        }
        in.close();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        Log.d(TAG, "||||||||||||||||||||| getGPSWeekFromServer runtime: "+duration);

        return Integer.parseInt(weekNum);
    }

    //Source: http://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
    public static void URLReader(String system, DBSQLiteHelper myDBHelper, int gpsWeek,
                                     String targetURL) throws Exception {
        long startTime = System.nanoTime();
        URL oracle = new URL(targetURL);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        Set<String> fileNameSet = new HashSet<String>();
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains(".sp3.Z") && (inputLine.contains("igu") || inputLine.contains("igv"))) {
                String subs = inputLine.substring(inputLine.indexOf("href") + 6, inputLine.indexOf("><img src=") - 1);
                if (!fileNameSet.contains(subs)) {
                    fileNameSet.add(subs);
                    DownloadFile(system, myDBHelper, gpsWeek, targetURL, subs);
                }
            }
        }
        in.close();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        Log.d(TAG, "||||||||||||||||||||| URLReader runtime(+DownloadFile): "+duration);
    }

    //Source: http://www.coderanch.com/t/613782/java-io/java/download-Zip-file
    public static void DownloadFile(String system, DBSQLiteHelper myDBHelper, int gpsWeek,
                                    String sourceurl, String filename) throws Exception {
        long startTime = System.nanoTime();
        int allCt = myDBHelper.getVehiclesCount();
        Runtime runtime = Runtime.getRuntime();
        Log.d(TAG, "||||||||||||||||||||| ("+allCt+" | "+runtime.freeMemory()+") In DownloadFile("+sourceurl+filename+")... ");

        URL url = new URL(sourceurl + filename);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputstream = connection.getInputStream();
        FileOutputStream outputstream = new FileOutputStream
                (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+filename);
        LZCInputStream lzc = new LZCInputStream(inputstream);
        lzc.uncompress(inputstream, outputstream);
        inputstream.close();

        String fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+filename;
        inputstream = new FileInputStream(fileLoc);
        InputStreamReader inputreader = new InputStreamReader(inputstream);
        BufferedReader in = new BufferedReader(inputreader);

        saveToVehicleObj(system, myDBHelper, gpsWeek, in, filename);
        //saveToCSV(vList);

        in.close();
        File f = new File(fileLoc);
        f.delete();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        Log.d(TAG, "||||||||||||||||||||| DownloadFile runtime(+saveToVehicleObj): "+duration);
    }

    public static void saveToVehicleObj(String system, DBSQLiteHelper myDBHelper, int gpsWeek,
                                        BufferedReader in, String filename) throws IOException {
        long startTime = System.nanoTime();
        String inputLine;
        Calendar date = null;
        long dateLong = 0;
        int inputLineCount = 1;

        List<VehicleObj> vList = new ArrayList<VehicleObj>();
        while ((inputLine = in.readLine()) != null) {
            if (inputLineCount >= 23 && inputLine.indexOf("EOF") < 0 && inputLine.indexOf("  P   P") < 0) {
                if (inputLine.indexOf("*  ") == 0) {
                    date = new GregorianCalendar(
                            Integer.parseInt(inputLine.substring(3, 8).trim()), //year
                            Integer.parseInt(inputLine.substring(8, 11).trim()) - 1, //month
                            Integer.parseInt(inputLine.substring(11, 14).trim()), //day
                            Integer.parseInt(inputLine.substring(14, 17).trim()), //hour
                            Integer.parseInt(inputLine.substring(17, 20).trim())); //min
                    dateLong = SatDateUtils.formatCalToLong(date);

                    //TODO: convert from GPS Time to UTC, so as to enable
                    //      locale time tracking
                } else {
                    String vId = inputLine.substring(0, 4).trim();
                    VehicleObj v = new VehicleObj(
                            vId,
                            date,
                            Double.parseDouble(inputLine.substring(4, 18).trim()), //x
                            Double.parseDouble(inputLine.substring(18, 32).trim()), //y
                            Double.parseDouble(inputLine.substring(32, 46).trim()) //z
                    );
                    v.system = system;
                    v.fileName = filename;
                    v.lineNum = inputLineCount;
                    v.gpsWeek = gpsWeek;
                    v.debug = "line " + inputLineCount;
                    v.dateLong = dateLong;
                    if ((v.xcoordinate == 0 && v.ycoordinate == 0 && v.zcoordinate == 0) ||
                            (v.xcoordinate == 999999.999999 || v.ycoordinate == 999999.999999 || v.zcoordinate == 999999.999999)) {
                        v.latitude = 0;
                        v.longitude = 0;
                        v.health = VehicleObj.HEALTH_BAD;
                    }

                    //save to DB
                    if (v.xcoordinate != Double.NaN && v.ycoordinate != Double.NaN && v.zcoordinate != Double.NaN && v.latitude != Double.NaN && v.longitude != Double.NaN) {
                        v.health = VehicleObj.HEALTH_GOOD;
                        vList.add(v);
                    }
                }
            }
            inputLineCount++;
        }
        long startTime1 = System.nanoTime();
        myDBHelper.addVehicleList(vList);
        long endTime1 = System.nanoTime();
        long duration1 = endTime1 - startTime1;

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        Log.d(TAG, "||||||||||||||||||||| addVehicle runtime: " + duration1);
        Log.d(TAG, "||||||||||||||||||||| saveToVehicleObj runtime(addVehicle): " + duration);
    }
}

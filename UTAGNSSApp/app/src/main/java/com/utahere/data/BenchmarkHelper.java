package com.utahere.data;

import android.os.Environment;
import android.util.Log;

import com.tspoon.benchit.Benchit;
import com.utahere.math.xyz2geo;
import com.utahere.objects.VehicleObj;
import com.utahere.utils.SatDateUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Tsailing on 7/7/2015.
 */

public class BenchmarkHelper {

    private static final String TAG = BenchmarkHelper.class.getSimpleName();

    public static void runBenchmark(List<VehicleObj> vList) throws IOException {
        runAccBenchmark();
        runPerfBenchmark(vList);
    }

    public static void runAccBenchmark() throws IOException {
        List<VehicleObj> vList = getTestData();

        String fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/accBenchmark_results_reduced.csv";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc, true));

        String header = "x, y, z, " +
                        "lat(o), " +
                        "alt(o)," +
                        "lat(basic), " +
                        "alt(basic)," +
                        "lat(hirvo), " +
                        "alt(hirvo)," +
                        "its(hirvo)," +
                        "lat(torge), " +
                        "alt(torge)," +
                        "its(torge)," +
                        "lat(astro), " +
                        "alt(astro)," +
                        "its(astro)," +
                        "lat(bowrg), " +
                        "alt(bowrg)," +
                        System.getProperty("line.separator");
        out.write(header);

        Log.i(TAG, "--- ACCURACY BENCHMARK STARTED ---");

        Set<String> checkUniqueSet = new HashSet<String>();
        for (int i = 0; i < vList.size(); i++) {
            VehicleObj v = vList.get(i);
            String unique = v.xcoordinate+","+v.ycoordinate+","+v.zcoordinate;
            if(!checkUniqueSet.contains(unique)) {
                xyz2geo.xyz2geo_basic2(v);
                xyz2geo.xyz2geo_hirvonenMoritz(v);
                xyz2geo.xyz2geo_torge(v);
                xyz2geo.xyz2geo_astroAlmanac(v);
                xyz2geo.xyz2geo_bowring(v);
                String s = createAccBenchmarkString(vList.size(), v);
                out.write(s);
                checkUniqueSet.add(unique);
            }
        }

        Log.i(TAG, "--- ACCURACY BENCHMARK DONE! ---");
        out.close();
    }

    private static List<VehicleObj> getTestData() throws IOException {

        String fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/testdata.csv";
        java.io.InputStream inputstream = new FileInputStream(fileLoc);
        InputStreamReader inputreader = new InputStreamReader(inputstream);
        BufferedReader in = new BufferedReader(inputreader);

        List<VehicleObj> vList = new ArrayList<>();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            if(inputLine.indexOf("#")<0) {
                VehicleObj v = new VehicleObj();
                int indexOf_a = inputLine.indexOf(",");
                v.xcoordinate = Double.parseDouble(inputLine.substring(0, indexOf_a).trim());
                int indexOf_b = inputLine.indexOf(",", indexOf_a + 1);
                v.ycoordinate = Double.parseDouble(inputLine.substring(indexOf_a + 1, indexOf_b).trim());
                indexOf_a = indexOf_b;
                indexOf_b = inputLine.indexOf(",", indexOf_a + 1);
                v.zcoordinate = Double.parseDouble(inputLine.substring(indexOf_a + 1, indexOf_b).trim());
                indexOf_a = indexOf_b;
                indexOf_b = inputLine.indexOf(",", indexOf_a + 1);
                v.longitude = Double.parseDouble(inputLine.substring(indexOf_a + 1, indexOf_b).trim());
                indexOf_a = indexOf_b;
                indexOf_b = inputLine.indexOf(",", indexOf_a + 1);
                v.latitude = Double.parseDouble(inputLine.substring(indexOf_a + 1, indexOf_b).trim());
                indexOf_a = indexOf_b;
                indexOf_b = inputLine.length();
                v.altitude = Double.parseDouble(inputLine.substring(indexOf_a + 1, indexOf_b).trim());
                vList.add(v);
            }
        }
        in.close();
        return vList;
    }

    private static String createAccBenchmarkString(int size, VehicleObj v) {
        return  v.xcoordinate + ", " +
                v.ycoordinate + ", " +
                v.zcoordinate + ", " +
                v.latitude + ", " +
                v.altitude + ", " +
                v.latitude_basic + ", " +
                v.altitude_basic + ", " +
                v.latitude_hirvonenMoritz + ", " +
                v.altitude_hirvonenMoritz + ", " +
                v.numOfIterations_hirvonenMoritz + ", " +
                v.latitude_torge + ", " +
                v.altitude_torge + ", " +
                v.numOfIterations_torge + ", " +
                v.latitude_astroAlmanac + ", " +
                v.altitude_astroAlmanac + ", " +
                v.numOfIterations_torge + ", " +
                v.latitude_bowring + ", " +
                v.altitude_bowring +
                System.getProperty("line.separator");
    }

    public static void runPerfBenchmark(List<VehicleObj> vList) throws IOException {
        String fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/perfBenchmark_results_reduced.csv";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc, true));

        String header =
                "date, " +
                        "algorithm, " +
                        "dataset, " +
                        "timeSpent (ms), " +
                        "memorySpent" +
                        System.getProperty("line.separator");
        out.write(header);

        Log.i(TAG, "--- PERFORMANCE BENCHMARK STARTED ---");

        for(int j=0; j<1000; j++) {
            //Basic method:
            Runtime runtime = Runtime.getRuntime();
            long startTime = System.nanoTime();
            Benchit.begin("Basic");
            for (int i = 0; i < vList.size(); i++) {
                VehicleObj v = vList.get(i);
                xyz2geo.xyz2geo_basic2(v);
            }
            Benchit.end("Basic").log();
            Benchit.analyze("Basic").log();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime); // Divide by 1000000 to get milliseconds
            long memory = runtime.totalMemory() - runtime.freeMemory();
            String s = createBenchmarkString("basic", vList.size(), duration, memory);
            out.write(s);
            System.gc();

            //Hirvonen and Moritz Method
            runtime = Runtime.getRuntime();
            startTime = System.nanoTime();
            Benchit.begin("Hirvonen");
            for (int i = 0; i < vList.size(); i++) {
                VehicleObj v = vList.get(i);
                xyz2geo.xyz2geo_hirvonenMoritz(v);
            }
            Benchit.end("Hirvonen").log();
            Benchit.analyze("Hirvonen").log();
            endTime = System.nanoTime();
            duration = (endTime - startTime); // Divide by 1000000 to get milliseconds
            memory = runtime.totalMemory() - runtime.freeMemory();
            s = createBenchmarkString("hirvonenMoritz", vList.size(), duration, memory);
            out.write(s);
            System.gc();

            //Torge Method / Heiskanen and Moritz Method
            runtime = Runtime.getRuntime();
            startTime = System.nanoTime();
            Benchit.begin("Torge");
            for (int i = 0; i < vList.size(); i++) {
                VehicleObj v = vList.get(i);
                xyz2geo.xyz2geo_torge(v);
            }
            Benchit.end("Torge").log();
            Benchit.analyze("Torge").log();
            endTime = System.nanoTime();
            duration = (endTime - startTime); // Divide by 1000000 to get milliseconds
            memory = runtime.totalMemory() - runtime.freeMemory();
            s = createBenchmarkString("torge", vList.size(), duration, memory);
            out.write(s);
            System.gc();

            //Astronomical Almanac 2002 Method
            runtime = Runtime.getRuntime();
            startTime = System.nanoTime();
            Benchit.begin("Astro");
            for (int i = 0; i < vList.size(); i++) {
                VehicleObj v = vList.get(i);
                xyz2geo.xyz2geo_astroAlmanac(v);
            }
            Benchit.end("Astro").log();
            Benchit.analyze("Astro").log();
            endTime = System.nanoTime();
            duration = (endTime - startTime); // Divide by 1000000 to get milliseconds
            memory = runtime.totalMemory() - runtime.freeMemory();
            s = createBenchmarkString("astroAlmanac", vList.size(), duration, memory);
            out.write(s);
            System.gc();

            //Bowring Method
            runtime = Runtime.getRuntime();
            startTime = System.nanoTime();
            Benchit.begin("Bowring");
            for (int i = 0; i < vList.size(); i++) {
                VehicleObj v = vList.get(i);
                xyz2geo.xyz2geo_bowring(v);
            }
            Benchit.end("Bowring").log();
            Benchit.analyze("Bowring").log();
            endTime = System.nanoTime();
            duration = (endTime - startTime); // Divide by 1000000 to get milliseconds
            memory = runtime.totalMemory() - runtime.freeMemory();
            s = createBenchmarkString("bowring", vList.size(), duration, memory);
            out.write(s);
            System.gc();
        }

        // Compare & print results of all Benchmarks
        Benchit.compare(Benchit.Stat.STANDARD_DEVIATION).log();
        // Compare & print results of specific Benchmarks
        Benchit.compare(Benchit.Stat.RANGE, Benchit.Order.ASCENDING, "benchmark-one", "benchmark-two").log();

        Log.i(TAG, "--- PERFORMANCE BENCHMARK DONE! ---");
        out.close();
    }

    private static String createBenchmarkString(String algorithm, int size, long duration, long memory) {
        return  SatDateUtils.formatLongToDate(System.currentTimeMillis()) + ", " +
                algorithm + ", " +
                size + ", " +
                duration + ", " +
                memory +
                System.getProperty("line.separator");
    }
}
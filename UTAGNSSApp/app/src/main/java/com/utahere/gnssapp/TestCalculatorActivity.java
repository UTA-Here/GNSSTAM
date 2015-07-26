package com.utahere.gnssapp;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.utahere.math.PositionCalculations;
import com.utahere.math.xyz2geo;
import com.utahere.objects.VehicleObj;

public class TestCalculatorActivity extends BaseActivity {
    private static final String TAG = TestCalculatorActivity.class.getSimpleName();

    //TODO: kill activity
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.testcalc_main);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }

    public void ButtonOnClick_geo2xyz(View v) {
        setContentView(R.layout.testcalc_sub_geo2xyz);
    }

    public void submit_geo2xyz(View button) {
        final EditText lat = (EditText) findViewById(R.id.EditLatitude);
        final EditText lng = (EditText) findViewById(R.id.EditLongitude);
        final EditText alt = (EditText) findViewById(R.id.EditAltitude);
        try {
            double latd = Double.parseDouble(lat.getText().toString());
            double lngd = Double.parseDouble(lng.getText().toString());
            double altd = Double.parseDouble(alt.getText().toString());

            VehicleObj v = new VehicleObj();
            v.setLatitude(latd);
            v.setLongitude(lngd);
            v.setAltitude(altd);
            PositionCalculations.geo2xyz(v);
            final TextView textViewToChange = (TextView) findViewById(R.id.result);
            textViewToChange.setText("Result: " +
                            "\r\nx: " + v.getXcoordinate() +
                            "\r\ny: " + v.getYcoordinate() +
                            "\r\nz: " + v.getZcoordinate()
            );
        } catch (java.lang.NumberFormatException e) {
            Toast.makeText(getApplicationContext(),
                    "Invalid input(s). Please verify and try again!",
                    Toast.LENGTH_LONG).show();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(lng.getWindowToken(), 0);
    }

    public void ButtonOnClick_xyz2geo(View v) {
        setContentView(R.layout.testcalc_sub_xyz2geo);
    }

    public void submit_xyz2geo(View button) {
        final EditText x = (EditText) findViewById(R.id.EditXCoordinate);
        final EditText y = (EditText) findViewById(R.id.EditYCoordinate);
        final EditText z = (EditText) findViewById(R.id.EditZCoordinate);

        // get selected radio button from radioGroup
        RadioGroup radioMethodsGroup = (RadioGroup) findViewById(R.id.radioMethods);
        int selectedId = radioMethodsGroup.getCheckedRadioButtonId();

        try {
            double xd = Double.parseDouble(x.getText().toString());
            double yd = Double.parseDouble(y.getText().toString());
            double zd = Double.parseDouble(z.getText().toString());

            VehicleObj v = new VehicleObj();
            v.setXcoordinate(xd);
            v.setYcoordinate(yd);
            v.setZcoordinate(zd);

            v.longitude = xyz2geo.calculateLogitude(v);
            if(selectedId==R.id.radio_bm) xyz2geo.xyz2geo_basic(v);
            else if(selectedId==R.id.radio_hmm) xyz2geo.xyz2geo_hirvonenMoritz(v);
            else if(selectedId==R.id.radio_tm) xyz2geo.xyz2geo_torge(v);
            else if(selectedId==R.id.radio_aam) xyz2geo.xyz2geo_astroAlmanac(v);
            else if(selectedId==R.id.radio_bwm) xyz2geo.xyz2geo_bowring(v);

            final TextView textViewToChange = (TextView) findViewById(R.id.result);
            textViewToChange.setText("Result: " +
                            "\r\naltitude: " + v.getAltitude() +
                            "\r\nlatitude: " + v.getLatitude() +
                            "\r\nlongitude: " + v.getLongitude()
            );
        } catch (java.lang.NumberFormatException e) {
            Toast.makeText(getApplicationContext(),
                    "Invalid input(s). Please verify and try again!",
                    Toast.LENGTH_LONG).show();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(z.getWindowToken(), 0);
    }

    public void ButtonOnClick_xyz2enu(View v) {
        setContentView(R.layout.testcalc_sub_xyz2enu);
    }

    public void submit_xyz2enu(View button) {
        final EditText x = (EditText) findViewById(R.id.EditXCoordinate);
        final EditText y = (EditText) findViewById(R.id.EditYCoordinate);
        final EditText z = (EditText) findViewById(R.id.EditZCoordinate);
        try {
            double xd = Double.parseDouble(x.getText().toString());
            double yd = Double.parseDouble(y.getText().toString());
            double zd = Double.parseDouble(z.getText().toString());

            VehicleObj v = new VehicleObj();
            v.setXcoordinate(xd);
            v.setYcoordinate(yd);
            v.setZcoordinate(zd);
            PositionCalculations.xyz2enu(v);
            final TextView textViewToChange = (TextView) findViewById(R.id.result);
            textViewToChange.setText("Result: " +
                            "\r\neast: " + v.getEast() +
                            "\r\nnorth: " + v.getNorth() +
                            "\r\nup: " + v.getUp()
            );
            v.getENU().print(6, 6);
        } catch (java.lang.NumberFormatException e) {
            Toast.makeText(getApplicationContext(),
                    "Invalid input(s). Please verify and try again!",
                    Toast.LENGTH_LONG).show();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(z.getWindowToken(), 0);
    }

    public void ButtonOnClick_enu2xyz(View v) {
        setContentView(R.layout.testcalc_sub_enu2xyz);
    }

    public void submit_enu2xyz(View button) {
        final EditText e = (EditText) findViewById(R.id.EditEast);
        final EditText n = (EditText) findViewById(R.id.EditNorth);
        final EditText u = (EditText) findViewById(R.id.EditUp);
        final EditText lat = (EditText) findViewById(R.id.EditLatitude);
        final EditText lng = (EditText) findViewById(R.id.EditLongitude);
        try {
            double ed = Double.parseDouble(e.getText().toString());
            double nd = Double.parseDouble(n.getText().toString());
            double ud = Double.parseDouble(u.getText().toString());
            double latd = Double.parseDouble(lat.getText().toString());
            double lond = Double.parseDouble(lng.getText().toString());

            VehicleObj v = new VehicleObj();
            v.setEast(ed);
            v.setNorth(nd);
            v.setUp(ud);
            v.setLatitude(latd);
            v.setLongitude(lond);
            PositionCalculations.enu2xyz(v);
            final TextView textViewToChange = (TextView) findViewById(R.id.result);
            textViewToChange.setText("Result: " +
                            "\r\nx: " + v.getXcoordinate() +
                            "\r\ny: " + v.getYcoordinate() +
                            "\r\nz: " + v.getZcoordinate()
            );
        } catch (java.lang.NumberFormatException ex) {
            Toast.makeText(getApplicationContext(),
                    "Invalid input(s). Please verify and try again!",
                    Toast.LENGTH_LONG).show();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
    }

    public void ButtonOnClick_intsatpos(View v) {
        setContentView(R.layout.testcalc_sub_intsatpos);
    }

    public void submit_intsatpos(View button) {
        //TODO: write this method
    }

    public void ButtonOnClick_sateleazi(View v) {
        setContentView(R.layout.testcalc_sub_sateleazi);
    }

    public void submit_sateleazi(View button) {
        final EditText e = (EditText) findViewById(R.id.EditEast);
        final EditText n = (EditText) findViewById(R.id.EditNorth);
        final EditText u = (EditText) findViewById(R.id.EditUp);
        try {
            double ed = Double.parseDouble(e.getText().toString());
            double nd = Double.parseDouble(n.getText().toString());
            double ud = Double.parseDouble(u.getText().toString());

            VehicleObj v = new VehicleObj();
            v.setEast(ed);
            v.setNorth(nd);
            v.setUp(ud);
            PositionCalculations.SatelliteElevationAndAzimuth(v);
            final TextView textViewToChange = (TextView) findViewById(R.id.result);
            textViewToChange.setText("Result: " +
                            "\r\nelevation: " + v.getElevation() +
                            "\r\nazimuth: " + v.getAzimuth()
            );
        } catch (java.lang.NumberFormatException ex) {
            Toast.makeText(getApplicationContext(),
                    "Invalid input(s). Please verify and try again!",
                    Toast.LENGTH_LONG).show();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
    }

    public void ButtonOnClick_deg2rad(View v) {
        setContentView(R.layout.testcalc_sub_deg2rad);
    }

    public void submit_deg2rad(View button) {
        final EditText deg = (EditText) findViewById(R.id.EditDegrees);
        try {
            double degd = Double.parseDouble(deg.getText().toString());

            double radd = PositionCalculations.DegtoRad(degd);
            final TextView textViewToChange = (TextView) findViewById(R.id.result);
            textViewToChange.setText("Result: " +
                            "\r\nradians: " + radd
            );
        } catch (java.lang.NumberFormatException e) {
            Toast.makeText(getApplicationContext(),
                    "Invalid input(s). Please verify and try again!",
                    Toast.LENGTH_LONG).show();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(deg.getWindowToken(), 0);
    }

    public void ButtonOnClick_rad2deg(View v) {
        setContentView(R.layout.testcalc_sub_rad2deg);
    }

    public void submit_rad2deg(View button) {
        final EditText rad = (EditText) findViewById(R.id.EditRadians);
        try {
            double radd = Double.parseDouble(rad.getText().toString());

            double degd = PositionCalculations.RadtoDeg(radd);
            final TextView textViewToChange = (TextView) findViewById(R.id.result);
            textViewToChange.setText("Result: " +
                            "\r\ndegrees: " + degd
            );
        } catch (java.lang.NumberFormatException e) {
            Toast.makeText(getApplicationContext(),
                    "Invalid input(s). Please verify and try again!",
                    Toast.LENGTH_LONG).show();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rad.getWindowToken(), 0);
    }
}

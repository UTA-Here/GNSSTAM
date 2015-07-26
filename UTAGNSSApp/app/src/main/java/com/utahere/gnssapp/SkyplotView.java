package com.utahere.gnssapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.location.GpsSatellite;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.view.View;


public class SkyplotView extends View {

    private static final String TAG = SkyplotView.class.getSimpleName();

    private Paint mGridPaint;
    private Paint mTextPaint;
    private Paint mBackground;
    private Bitmap mSatelliteBitmapUsed;
    private Bitmap mSatelliteBitmapUnused;
    private Bitmap mSatelliteBitmapNoFix;
    private Bitmap mSatelliteBitmapTest;

    private float mBitmapAdjustment;

    android.location.GpsStatus gpsStatus = null;
    android.location.LocationManager lp = null;

    public SkyplotView(Context context) {
        this(context, null);
    }

    public SkyplotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkyplotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        lp = (android.location.LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        mGridPaint = new Paint();
        mGridPaint.setColor(0xFFDDDDDD);
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Style.STROKE);
        mGridPaint.setStrokeWidth(1.0f);
        mBackground = new Paint();
        mBackground.setColor(0xFF4444DD);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTextSize(20.0f);
        mTextPaint.setTextAlign(Align.CENTER);

        mSatelliteBitmapUsed = ((BitmapDrawable)getResources().getDrawable(R.drawable.satgreen)).getBitmap();
        mSatelliteBitmapUnused = ((BitmapDrawable)getResources().getDrawable(R.drawable.satyellow)).getBitmap();
        mSatelliteBitmapNoFix = ((BitmapDrawable)getResources().getDrawable(R.drawable.satred)).getBitmap();
        mSatelliteBitmapTest = ((BitmapDrawable)getResources().getDrawable(R.drawable.satblue)).getBitmap();
        mBitmapAdjustment = mSatelliteBitmapUsed.getHeight() / 2;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerY = getHeight() / 2;
        float centerX = getWidth() / 2;
        int minRadius = Math.min(getHeight(), getWidth());
        int radius = (int)(minRadius / 2) - 8;

        final Paint gridPaint = mGridPaint;
        final Paint textPaint = mTextPaint;
        canvas.drawPaint(mBackground);
        canvas.drawCircle(centerX, centerY, radius, gridPaint);
        canvas.drawCircle(centerX, centerY, radius * 3 / 4, gridPaint);
        canvas.drawCircle(centerX, centerY, radius >> 1, gridPaint);
        canvas.drawCircle(centerX, centerY, radius >> 2, gridPaint);
        canvas.drawLine(centerX, centerY - (radius >> 2), centerX, centerY - radius, gridPaint);
        canvas.drawLine(centerX, centerY + (radius >> 2) , centerX, centerY + radius, gridPaint);
        canvas.drawLine(centerX - (radius >> 2), centerY, centerX - radius, centerY, gridPaint);
        canvas.drawLine(centerX + (radius >> 2), centerY, centerX + radius, centerY, gridPaint);

        float scale = radius / 90.0f;
        if (lp.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            gpsStatus = this.lp.getGpsStatus(gpsStatus);

            float mX;
            float mY;

            for (GpsSatellite s: gpsStatus.getSatellites()){
                float theta = - (s.getAzimuth() + 90);
                float rad = (float) (theta * Math.PI/180.0f);
                mX = (float)Math.cos(rad);
                mY = -(float)Math.sin(rad);
                float elevation = s.getElevation() - 90.0f;
                if (elevation > 90 || s.getAzimuth() < 0 || s.getPrn() < 0){
                    continue;
                }
                float a = elevation * scale;

                int x = (int)Math.round(centerX + (mX * a) - mBitmapAdjustment);
                int y = (int)Math.round(centerY + (mY * a) - mBitmapAdjustment);
                if (s.usedInFix()){
                    canvas.drawBitmap(mSatelliteBitmapUsed, x, y, gridPaint);
                } else {
                    if (gpsStatus.getTimeToFirstFix() > 0){
                        canvas.drawBitmap(mSatelliteBitmapUnused, x, y, gridPaint);
                    } else {
                        canvas.drawBitmap(mSatelliteBitmapNoFix, x, y, gridPaint);
                    }
                }

                String t = Integer.toString(s.getPrn()); // + "+"+s.getAzimuth() + "+"+s.getElevation();
                canvas.drawText(t, x, y, textPaint);
            }
        }
    }
}
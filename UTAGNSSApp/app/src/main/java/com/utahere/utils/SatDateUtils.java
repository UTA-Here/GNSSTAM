package com.utahere.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tsailing on 3/13/2015.
 */
public class SatDateUtils {
    public static SimpleDateFormat iso8601SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat iso8601SimpleDateFormatWithTZ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public static String formatDateStr(String year, String month, String day, String hour, String min) {
        String dateformat = year + "-" +
                (month.length()<2?"0"+month:month) + "-" +
                (day.length()<2?"0"+day:day) + " " +
                (hour.length()<2?"0"+hour:hour) + ":" +
                (min.length()<2?"0"+min:min) + ":00";
        return dateformat;
    }

    public static long formatDaysToLong(int days) {
        return TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
    }

    public static String formatDateToStr(Date date) {
        return iso8601SimpleDateFormatWithTZ.format(date.getTime());
    }

    public static long formatCalToLong(Calendar c) {
        return c.getTimeInMillis();
    }

    public static Date formatLongToDate(long l) {
        return new Date(l);
    }

    public static Calendar formatStrToCal(String s) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(iso8601SimpleDateFormat.parse(s));

        return cal;
    }

    //Regionalized time formatter
    public static String formatDateTime(Context context, String timeToFormat) {

        String finalDateTime = "";

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = iso8601SimpleDateFormat.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                int flags = 0;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
                flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when + TimeZone.getDefault().getOffset(when), flags);
            }
        }
        return finalDateTime;
    }

    //Source: http://www.codeproject.com/Questions/468582/gps-week-day-of-week-and-gps-time
    public static int calculateGPSWeek(Calendar dCal) {
        double yr = dCal.getInstance().get(Calendar.YEAR);
        double mn = dCal.getInstance().get(Calendar.MONTH);
        double dy = dCal.getInstance().get(Calendar.DATE);
        double y = 0;
        double m = 0;
        double b = 0;
        double jd = 0;

        if (mn > 2)
        {
            y = yr;
            m = mn;
        }
        else
        {
            y = yr - 1;
            m = mn + 12;
        }

        double date0 = dy + 31 * (mn + 12 * yr);
        double date1 = 4 + 31 * (10 + 12 * 1582);
        double date2 = 15 + 31 * (10 + 12 * 1582);

        if (date0 < date1)
        {
            b = -2;
        }
        else
        {
            b = y / 400 - y / 100;
        }

        if ( y > 0)
        {
            jd = 365.25 * y + 30.6001 * (m + 1) + b + 1720996.5 + dy;// julijanski datum
        }
        else
        {
            jd = 365.25 * y - 0.75 + 30.6001 * (m + 1) + b + 1720996.5 + dy;// julijanski datum
        }

        double mjd = jd - 2400000.5;// modifikovvani julijanski datum

        // GPS nedelja racunam
        double jd1 = 0;
        yr = 1980;
        mn = 1;
        dy = 6;
        if (mn > 2)
        {
            y = yr;
            m = mn;
        }
        else
        {
            y = yr - 1;
            m = mn + 12;
        }
        date0 = dy + 31 * (mn + 12 * yr);
        date1 = 4 + 31 * (10 + 12 * 1582);
        date2 = 15 + 31 * (10 + 12 * 1582);

        if (date0 < date1)
        {
            b = -2;
        }
        else
        {
            b = y / 400 - y / 100;
        }

        if (y > 0)
        {
            jd1 = 365.25 * y + 30.6001 * (m + 1) + b + 1720996.5 + dy;// julijanski datum
        }
        else
        {
            jd1 = 365.25 * y - 0.75 + 30.6001 * (m + 1) + b + 1720996.5 + dy;// julijanski datum
        }
        //double jdgps = jd1 - 2400000.5;// modifikovani julijanski datum
        double gpsWeek = (jd - jd1) / 7;// gps nedelja
        //double sow = (jd - (jd1 + gpsWeek * 7)) * 3600 * 24;// sekunda u nedelji
        // kraj racunanja gps nedelje

        return (int)gpsWeek;
    }

    public static Map<TimeUnit,Long> computeDateDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);

        Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit,Long>();
        long milliesRest = diffInMillies;
        for ( TimeUnit unit : units ) {
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit,diff);
        }
        return result;
    }
}
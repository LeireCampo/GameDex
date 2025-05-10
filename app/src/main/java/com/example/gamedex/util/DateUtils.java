package com.example.gamedex.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String API_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "d 'de' MMMM 'de' yyyy";

    public static String formatApiDate(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());

            Date date = apiFormat.parse(apiDate);
            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            // Si hay error en el formato, devolver la fecha original
            return apiDate;
        }

        return apiDate;
    }
}
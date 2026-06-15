package com.mopr.personal_finance_manager.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private static SimpleDateFormat getMonthYearFormat() {
        return new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    }

    private static SimpleDateFormat getBudgetMonthFormat() {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    }

    public static String formatDate(long timestamp) {
        return getDateFormat().format(new Date(timestamp));
    }

    public static String formatMonthYear(long timestamp) {
        String result = getMonthYearFormat().format(new Date(timestamp));
        // Capitalize first letter (useful for some locales)
        if (!result.isEmpty()) {
            return result.substring(0, 1).toUpperCase() + result.substring(1);
        }
        return result;
    }

    public static String getBudgetMonth(long timestamp) {
        return getBudgetMonthFormat().format(new Date(timestamp));
    }

    public static String getCurrentBudgetMonth() {
        return getBudgetMonthFormat().format(new Date());
    }

    public static long getStartOfMonth(Calendar calendar) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfMonth(Calendar calendar) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
}

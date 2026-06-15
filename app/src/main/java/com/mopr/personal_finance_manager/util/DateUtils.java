package com.mopr.personal_finance_manager.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat budgetMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    public static String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    public static String formatMonthYear(long timestamp) {
        return monthYearFormat.format(new Date(timestamp));
    }

    public static String getBudgetMonth(long timestamp) {
        return budgetMonthFormat.format(new Date(timestamp));
    }

    public static String getCurrentBudgetMonth() {
        return budgetMonthFormat.format(new Date());
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

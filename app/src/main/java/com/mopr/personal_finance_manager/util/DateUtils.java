package com.mopr.personal_finance_manager.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // ── Format helpers ────────────────────────────────────────────────

    private static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private static SimpleDateFormat getMonthYearFormat() {
        return new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    }

    public static String formatDate(long timestamp) {
        return getDateFormat().format(new Date(timestamp));
    }

    public static String formatMonthYear(long timestamp) {
        String r = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date(timestamp));
        return r.isEmpty() ? r : r.substring(0, 1).toUpperCase() + r.substring(1);
    }

    // ── Period key builders ───────────────────────────────────────────

    /**
     * "2026-06"
     */
    public static String getMonthKey(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(cal.getTime());
    }

    public static String getCurrentMonthKey() {
        return getMonthKey(Calendar.getInstance());
    }

    /**
     * "2026-W24" (ISO week)
     */
    public static String getWeekKey(Calendar cal) {
        // ISO week: use Locale.US for consistent numbering
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-'W'ww", Locale.US);
        return sdf.format(cal.getTime());
    }

    public static String getCurrentWeekKey() {
        return getWeekKey(Calendar.getInstance());
    }

    /**
     * "2026-06-17"
     */
    public static String getDayKey(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
    }

    public static String getCurrentDayKey() {
        return getDayKey(Calendar.getInstance());
    }

    // ── Legacy compat (used all over the app) ────────────────────────

    /**
     * @deprecated use getCurrentMonthKey()
     */
    public static String getCurrentBudgetMonth() {
        return getCurrentMonthKey();
    }

    /**
     * @deprecated use getCurrentMonthKey()
     */
    public static String getBudgetMonth(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        return getMonthKey(c);
    }

    // ── Date range calculators ────────────────────────────────────────

    public static long getStartOfMonth(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);
        return startOfDay(c);
    }

    public static long getEndOfMonth(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endOfDay(c);
    }

    public static long getStartOfWeek(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.setMinimalDaysInFirstWeek(1);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return startOfDay(c);
    }

    public static long getEndOfWeek(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.setMinimalDaysInFirstWeek(1);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        // If this rolls back, advance a week
        if (c.before(cal)) c.add(Calendar.WEEK_OF_YEAR, 1);
        return endOfDay(c);
    }

    public static long getStartOfDay(Calendar cal) {
        return startOfDay((Calendar) cal.clone());
    }

    public static long getEndOfDay(Calendar cal) {
        return endOfDay((Calendar) cal.clone());
    }

    private static long startOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static long endOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    /**
     * Given a period type and key, return [startMs, endMs].
     * Used by BudgetFragment to query matching transactions.
     */
    public static long[] getRangeForPeriod(String periodType, String periodKey) {
        Calendar cal = Calendar.getInstance();
        switch (periodType) {
            case "DAY": {
                // periodKey = "yyyy-MM-dd"
                try {
                    Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(periodKey);
                    cal.setTime(d);
                } catch (Exception ignored) {
                }
                return new long[]{startOfDay(cal), endOfDay(cal)};
            }
            case "WEEK": {
                // periodKey = "yyyy-Www"
                try {
                    // Parse "2026-W24" → set week
                    String[] parts = periodKey.split("-W");
                    int year = Integer.parseInt(parts[0]);
                    int week = Integer.parseInt(parts[1]);
                    cal.clear();
                    cal.setMinimalDaysInFirstWeek(1);
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.WEEK_OF_YEAR, week);
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } catch (Exception ignored) {
                }
                return new long[]{startOfDay(cal), endOfWeek(cal)};
            }
            default: {
                // MONTH: periodKey = "yyyy-MM"
                try {
                    String[] parts = periodKey.split("-");
                    cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                    cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                } catch (Exception ignored) {
                }
                return new long[]{getStartOfMonth(cal), getEndOfMonth(cal)};
            }
        }
    }

    private static long endOfWeek(Calendar startCal) {
        Calendar c = (Calendar) startCal.clone();
        c.add(Calendar.DAY_OF_YEAR, 6); // Mon + 6 = Sun
        return endOfDay(c);
    }

    // ── Display label builders ────────────────────────────────────────

    /**
     * Returns a human-readable label for the period selector header.
     * e.g. "Jun 01–30, 2026" / "Jun 16–22, 2026" / "Jun 17, 2026"
     */
    public static String getPeriodDisplayLabel(String periodType, String periodKey) {
        long[] range = getRangeForPeriod(periodType, periodKey);
        Calendar s = Calendar.getInstance();
        s.setTimeInMillis(range[0]);
        Calendar e = Calendar.getInstance();
        e.setTimeInMillis(range[1]);
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int sm = s.get(Calendar.MONTH), em = e.get(Calendar.MONTH);
        int sy = s.get(Calendar.YEAR);
        if ("DAY".equals(periodType)) {
            return months[sm] + " " + s.get(Calendar.DAY_OF_MONTH) + ", " + sy;
        }
        if (sm == em) {
            return months[sm] + " " + s.get(Calendar.DAY_OF_MONTH)
                + "–" + e.get(Calendar.DAY_OF_MONTH) + ", " + sy;
        }
        return months[sm] + " " + s.get(Calendar.DAY_OF_MONTH)
            + "–" + months[em] + " " + e.get(Calendar.DAY_OF_MONTH) + ", " + sy;
    }
}

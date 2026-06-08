package com.mopr.personal_finance_manager.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final Locale VIETNAM = new Locale("vi", "VN");
    private static final Locale US = Locale.US;

    /**
     * Format as Vietnamese Dong (default).
     */
    public static String formatVND(double amount) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(VIETNAM);
        return fmt.format(amount);
    }

    /**
     * Format as USD.
     */
    public static String formatUSD(double amount) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(US);
        return fmt.format(amount);
    }

    /**
     * Format by explicit currency code — "VND" or "USD".
     */
    public static String format(double amount, String currencyCode) {
        if ("USD".equalsIgnoreCase(currencyCode)) return formatUSD(amount);
        return formatVND(amount);
    }
}

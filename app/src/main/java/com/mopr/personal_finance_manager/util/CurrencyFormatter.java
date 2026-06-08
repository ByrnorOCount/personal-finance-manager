package com.mopr.personal_finance_manager.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final Locale VIETNAM = new Locale("vi", "VN");

    public static String formatVND(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(VIETNAM);
        return format.format(amount);
    }
}

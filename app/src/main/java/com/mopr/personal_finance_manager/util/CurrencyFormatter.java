package com.mopr.personal_finance_manager.util;

import java.text.NumberFormat;
import java.util.Currency;

/**
 * Utility class for formatting currency values.
 * Standardizes money string representation across the app.
 */
public class CurrencyFormatter {

    /**
     * Formats a double value as a currency string using the default locale.
     *
     * @param amount The amount to format.
     * @return A formatted currency string (e.g., "$1,234.56").
     */
    public static String format(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    /**
     * Formats a double value as a currency string using a specific currency code.
     *
     * @param amount       The amount to format.
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR").
     * @return A formatted currency string.
     */
    public static String format(double amount, String currencyCode) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        try {
            format.setCurrency(Currency.getInstance(currencyCode));
        } catch (IllegalArgumentException e) {
            // Fallback to default if currency code is valid
        }
        return format.format(amount);
    }

    /**
     * Formats an amount in cents as a currency string.
     * Storing money as cents (long) is recommended to avoid floating point precision issues.
     * @param cents The amount in cents.
     * @return A formatted currency string.
     */
    public static String formatCents(long cents) {
        return format(cents / 100.0);
    }

    /**
     * Formats an amount in cents with a specific currency.
     * @param cents The amount in cents.
     * @param currencyCode The ISO 4217 currency code.
     * @return A formatted currency string.
     */
    public static String formatCents(long cents, String currencyCode) {
        return format(cents / 100.0, currencyCode);
    }

    /**
     * Parses a currency string into a double.
     * @param amountString The string to parse.
     * @return The parsed double value.
     * @throws java.text.ParseException If the string cannot be parsed.
     */
    public static double parse(String amountString) throws java.text.ParseException {
        Number number = NumberFormat.getCurrencyInstance().parse(amountString);
        return number != null ? number.doubleValue() : 0.0;
    }
}

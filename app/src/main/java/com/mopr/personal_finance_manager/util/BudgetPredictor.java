package com.mopr.personal_finance_manager.util;

import com.mopr.personal_finance_manager.data.model.PredictionResult;

import java.util.List;
import java.util.Locale;

public class BudgetPredictor {

    /**
     * Analyzes the current period and predicts end-of-period status.
     */
    public static PredictionResult analyzeCurrentPeriod(double currentSpend, double budgetLimit, long startMs, long endMs) {
        long now = System.currentTimeMillis();

        // Ensure now is within the range
        if (now < startMs) now = startMs;
        if (now > endMs) now = endMs;

        long totalDuration = endMs - startMs;
        long elapsedDuration = now - startMs;
        long remainingDuration = endMs - now;

        // Convert to days for better readability (minimum 1 day to avoid div by zero)
        int elapsedDays = (int) Math.max(1, elapsedDuration / (1000 * 60 * 60 * 24));
        int totalDays = (int) Math.max(1, totalDuration / (1000 * 60 * 60 * 24));
        int daysRemaining = totalDays - elapsedDays;

        double dailyBurnRate = currentSpend / elapsedDays;
        double projectedEndSpend = currentSpend + (dailyBurnRate * daysRemaining);

        PredictionResult.OverrunRisk risk = PredictionResult.OverrunRisk.LOW;
        String recommendation = "You're doing great! You have " +
            String.format(Locale.getDefault(), "%.0f%%", (1 - (currentSpend/budgetLimit)) * 100) +
            " of your budget left with " +
            String.format(Locale.getDefault(), "%.0f%%", ((double)daysRemaining/totalDays) * 100) +
            " of the month remaining.";

        if (budgetLimit > 0) {
            double spendPercent = currentSpend / budgetLimit;
            double timePercent = (double) elapsedDays / totalDays;

            if (spendPercent > timePercent + 0.15) {
                risk = PredictionResult.OverrunRisk.HIGH;
                recommendation = String.format(Locale.getDefault(), "Alert: You're spending faster than usual (%.0f%% of budget used in %.0f%% of time). Try to cut back on %s.",
                    spendPercent * 100, timePercent * 100, "non-essentials");
            } else if (spendPercent > timePercent) {
                risk = PredictionResult.OverrunRisk.MEDIUM;
                recommendation = "You're slightly ahead of your budget. Consider reviewing your recent shopping or food expenses.";
            }

            double projectedPercent = projectedEndSpend / budgetLimit;
            if (projectedPercent > 1.0) {
                risk = PredictionResult.OverrunRisk.HIGH;
                recommendation = String.format(Locale.getDefault(), "Warning: At this rate, you will exceed your budget by %.0f ₫ (%.0f%% overhead).",
                    projectedEndSpend - budgetLimit, (projectedPercent - 1) * 100);
            }
        } else {
            recommendation = "Set a budget limit to get personalized spending advice and risk alerts.";
        }

        return new PredictionResult(currentSpend, projectedEndSpend, budgetLimit, dailyBurnRate, daysRemaining, risk, recommendation);
    }

    /**
     * Simple forecasting for next period based on historical data using moving average.
     */
    public static double predictNextPeriodExpense(List<Double> historicalActuals) {
        if (historicalActuals == null || historicalActuals.isEmpty()) return 0;

        double sum = 0;
        int count = historicalActuals.size();

        // Give more weight to recent periods (Weighted Moving Average)
        double totalWeight = 0;
        for (int i = 0; i < count; i++) {
            double weight = i + 1; // Recent has more weight
            sum += historicalActuals.get(i) * weight;
            totalWeight += weight;
        }

        return sum / totalWeight;
    }

    /**
     * Detects if spending trend is increasing or decreasing.
     */
    public static double calculateTrend(List<Double> historicalActuals) {
        if (historicalActuals == null || historicalActuals.size() < 2) return 0;

        int n = historicalActuals.size();
        double last = historicalActuals.get(n - 1);
        double previous = historicalActuals.get(n - 2);

        if (previous == 0) return 0;
        return (last - previous) / previous;
    }

    /**
     * Identifies transactions that are significantly higher than the average for their category.
     */
    public static List<com.mopr.personal_finance_manager.data.local.Transaction> detectAnomalies(List<com.mopr.personal_finance_manager.data.local.Transaction> recentTransactions, double categoryAverage) {
        java.util.List<com.mopr.personal_finance_manager.data.local.Transaction> anomalies = new java.util.ArrayList<>();
        if (categoryAverage <= 0) return anomalies;

        for (com.mopr.personal_finance_manager.data.local.Transaction t : recentTransactions) {
            if (t.amount > categoryAverage * 2.5) { // 2.5x the average is a simple anomaly threshold
                anomalies.add(t);
            }
        }
        return anomalies;
    }
}

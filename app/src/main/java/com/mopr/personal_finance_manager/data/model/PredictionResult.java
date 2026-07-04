package com.mopr.personal_finance_manager.data.model;

import java.util.List;

public class PredictionResult {
    public double currentSpend;
    public double projectedEndSpend;
    public double budgetLimit;
    public double dailyBurnRate;
    public int daysRemaining;
    public OverrunRisk risk;
    public String recommendation;

    public enum OverrunRisk {
        LOW, MEDIUM, HIGH
    }

    public PredictionResult(double currentSpend, double projectedEndSpend, double budgetLimit,
                            double dailyBurnRate, int daysRemaining, OverrunRisk risk, String recommendation) {
        this.currentSpend = currentSpend;
        this.projectedEndSpend = projectedEndSpend;
        this.budgetLimit = budgetLimit;
        this.dailyBurnRate = dailyBurnRate;
        this.daysRemaining = daysRemaining;
        this.risk = risk;
        this.recommendation = recommendation;
    }
}

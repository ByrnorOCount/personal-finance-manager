package com.mopr.personal_finance_manager.data.model;

import com.mopr.personal_finance_manager.data.local.Budget;

public class CategoryBudgetUI {
    public String category;
    public double budgetLimit;
    public double spentAmount;

    public CategoryBudgetUI(String category, double budgetLimit, double spentAmount) {
        this.category = category;
        this.budgetLimit = budgetLimit;
        this.spentAmount = spentAmount;
    }

    public double getRemaining() {
        return Math.max(0, budgetLimit - spentAmount);
    }

    public int getProgress() {
        if (budgetLimit == 0) return 0;
        return (int) Math.min(100, (spentAmount / budgetLimit) * 100);
    }
}

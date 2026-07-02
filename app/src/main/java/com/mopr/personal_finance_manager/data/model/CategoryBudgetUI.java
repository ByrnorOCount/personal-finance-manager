package com.mopr.personal_finance_manager.data.model;

import com.mopr.personal_finance_manager.data.local.Budget;

public class CategoryBudgetUI {
    public int categoryId;
    public String categoryName;
    public int iconRes;
    public int colorRes;
    public double budgetLimit;
    public double spentAmount;

    public CategoryBudgetUI(int categoryId, String categoryName, int iconRes, int colorRes, double budgetLimit, double spentAmount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
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

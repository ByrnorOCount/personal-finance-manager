package com.mopr.personal_finance_manager.data.model;

import com.mopr.personal_finance_manager.R;

import java.io.Serializable;

public class CategoryBudgetUI implements Serializable {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public int viewType = TYPE_ITEM;
    public int categoryId;
    public String categoryName;
    public int iconRes;
    public int colorRes;
    public double budgetLimit;
    public double spentAmount;
    public String type; // INCOME or EXPENSE
    public String note;

    // For headers
    public double totalBudgetedForSection;
    public double totalSpentForSection;

    public CategoryBudgetUI(int categoryId, String categoryName, int iconRes, int colorRes, double budgetLimit, double spentAmount, String type, String note) {
        this.viewType = TYPE_ITEM;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.budgetLimit = budgetLimit;
        this.spentAmount = spentAmount;
        this.type = type;
        this.note = note;
    }

    public CategoryBudgetUI(String sectionName, double totalBudgeted, double totalSpent) {
        this.viewType = TYPE_HEADER;
        this.categoryName = sectionName;
        this.totalBudgetedForSection = totalBudgeted;
        this.totalSpentForSection = totalSpent;
    }

    public double getRemaining() {
        return Math.max(0, budgetLimit - spentAmount);
    }

    public int getProgress() {
        if (budgetLimit == 0) return 0;
        return (int) Math.min(100, (spentAmount / budgetLimit) * 100);
    }
}

package com.mopr.personal_finance_manager.data.model;

import android.content.Context;
import com.mopr.personal_finance_manager.R;

public class Category {
    public static final String SALARY = "Salary";
    public static final String FREELANCE = "Freelance";
    public static final String INVESTMENT = "Investment";
    public static final String GIFT = "Gift";

    public static final String FOOD = "Food";
    public static final String TRANSPORT = "Transport";
    public static final String BILLS = "Bills";
    public static final String SHOPPING = "Shopping";
    public static final String HEALTH = "Health";
    public static final String ENTERTAINMENT = "Entertainment";
    public static final String OTHER = "Other";

    public static String getDisplayName(Context ctx, String category) {
        return category; // For now
    }

    public static int getIconRes(String category) {
        if (category == null) return R.drawable.ic_cat_other;
        switch (category) {
            case SALARY: return R.drawable.ic_cat_salary;
            case FREELANCE: return R.drawable.ic_cat_freelance;
            case INVESTMENT: return R.drawable.ic_cat_investment;
            case GIFT: return R.drawable.ic_cat_gift;
            case FOOD: return R.drawable.ic_cat_food;
            case TRANSPORT: return R.drawable.ic_cat_transport;
            case BILLS: return R.drawable.ic_cat_bills;
            case SHOPPING: return R.drawable.ic_cat_shopping;
            case HEALTH: return R.drawable.ic_cat_health;
            case ENTERTAINMENT: return R.drawable.ic_cat_entertainment;
            default: return R.drawable.ic_cat_other;
        }
    }

    public static int getColorRes(String category) {
        if (category == null) return R.color.cat_other;
        switch (category) {
            case SALARY:
            case FREELANCE:
            case INVESTMENT:
            case GIFT:
                return R.color.income_green;
            case FOOD: return R.color.cat_food;
            case TRANSPORT: return R.color.cat_transport;
            case BILLS: return R.color.cat_bills;
            case SHOPPING: return R.color.cat_shopping;
            case HEALTH: return R.color.cat_health;
            case ENTERTAINMENT: return R.color.cat_entertainment;
            default: return R.color.cat_other;
        }
    }
}

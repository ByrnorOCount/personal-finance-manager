package com.mopr.personal_finance_manager.data.model;

import android.content.Context;
import com.mopr.personal_finance_manager.R;

public class Category {
    public static final String SALARY = "Salary";
    public static final String FREELANCE = "Freelance";
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
        switch (category) {
            case SALARY: return R.drawable.ic_cat_food; // TODO: replace with proper icons
            case FOOD: return R.drawable.ic_cat_food;
            case TRANSPORT: return R.drawable.ic_cat_food;
            default: return R.drawable.ic_cat_food;
        }
    }

    public static int getColorRes(String category) {
        switch (category) {
            case SALARY: return R.color.income_green;
            case FOOD: return R.color.cat_food;
            case TRANSPORT: return R.color.cat_transport;
            case BILLS: return R.color.cat_bills;
            default: return R.color.cat_other;
        }
    }
}

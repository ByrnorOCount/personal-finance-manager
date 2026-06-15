package com.mopr.personal_finance_manager.data.model;

import android.content.Context;
import com.mopr.personal_finance_manager.R;

// Not a Room entity
public class Category {

    // Expense categories
    public static final String FOOD          = "FOOD";
    public static final String TRANSPORT     = "TRANSPORT";
    public static final String BILLS         = "BILLS";
    public static final String SHOPPING      = "SHOPPING";
    public static final String HEALTH        = "HEALTH";
    public static final String ENTERTAINMENT = "ENTERTAINMENT";
    public static final String OTHER         = "OTHER";

    // Income categories
    public static final String SALARY        = "SALARY";
    public static final String FREELANCE     = "FREELANCE";
    public static final String INVESTMENT    = "INVESTMENT";
    public static final String GIFT          = "GIFT";
    public static final String OTHER_INCOME  = "OTHER_INCOME";

    // Display names from resources
    public static String getDisplayName(Context context, String key) {
        int resId;
        switch (key) {
            case FOOD:          resId = R.string.cat_food; break;
            case TRANSPORT:     resId = R.string.cat_transport; break;
            case BILLS:         resId = R.string.cat_bills; break;
            case SHOPPING:      resId = R.string.cat_shopping; break;
            case HEALTH:        resId = R.string.cat_health; break;
            case ENTERTAINMENT: resId = R.string.cat_entertainment; break;
            case OTHER:         resId = R.string.cat_other; break;
            case SALARY:        resId = R.string.cat_salary; break;
            case FREELANCE:     resId = R.string.cat_freelance; break;
            case INVESTMENT:    resId = R.string.cat_investment; break;
            case GIFT:          resId = R.string.cat_gift; break;
            case OTHER_INCOME:  resId = R.string.cat_other_income; break;
            default:            return key;
        }
        return context.getString(resId);
    }

    // Vector icon resource ID per category
    public static int getIconRes(String key) {
        switch (key) {
            case FOOD:          return R.drawable.ic_cat_food;
            case TRANSPORT:     return R.drawable.ic_cat_transport;
            case BILLS:         return R.drawable.ic_cat_bills;
            case SHOPPING:      return R.drawable.ic_cat_shopping;
            case HEALTH:        return R.drawable.ic_cat_health;
            case ENTERTAINMENT: return R.drawable.ic_cat_entertainment;
            case SALARY:        return R.drawable.ic_cat_salary;
            case FREELANCE:     return R.drawable.ic_cat_freelance;
            case INVESTMENT:    return R.drawable.ic_cat_investment;
            case GIFT:          return R.drawable.ic_cat_gift;
            default:            return R.drawable.ic_cat_other;
        }
    }

    // Color resource for each category
    public static int getColorRes(String key) {
        switch (key) {
            case FOOD:          return R.color.cat_food;
            case TRANSPORT:     return R.color.cat_transport;
            case BILLS:         return R.color.cat_bills;
            case SHOPPING:      return R.color.cat_shopping;
            case HEALTH:        return R.color.cat_health;
            case ENTERTAINMENT: return R.color.cat_entertainment;
            default:            return R.color.cat_other;
        }
    }

    // Expense category list (for Add Transaction screen spinner)
    public static String[] expenseCategories() {
        return new String[]{FOOD, TRANSPORT, BILLS, SHOPPING, HEALTH, ENTERTAINMENT, OTHER};
    }

    // Income category list
    public static String[] incomeCategories() {
        return new String[]{SALARY, FREELANCE, INVESTMENT, GIFT, OTHER_INCOME};
    }
}

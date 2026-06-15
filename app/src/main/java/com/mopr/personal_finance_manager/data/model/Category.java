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

    // Emoji icon per category for list rows
    public static String getIcon(String key) {
        switch (key) {
            case FOOD:          return "🍔";
            case TRANSPORT:     return "🚗";
            case BILLS:         return "💡";
            case SHOPPING:      return "🛍️";
            case HEALTH:        return "💊";
            case ENTERTAINMENT: return "🎬";
            case SALARY:        return "💼";
            case FREELANCE:     return "💻";
            case INVESTMENT:    return "📈";
            case GIFT:          return "🎁";
            default:            return "💰";
        }
    }

    // Color resource for each category (used in charts and badges)
    // Returns a hex color string
    public static String getColor(String key) {
        switch (key) {
            case FOOD:          return "#FF6B6B";
            case TRANSPORT:     return "#4ECDC4";
            case BILLS:         return "#FFE66D";
            case SHOPPING:      return "#A78BFA";
            case HEALTH:        return "#6BCB77";
            case ENTERTAINMENT: return "#F4A261";
            case OTHER:         return "#AAAAAA";
            case SALARY:        return "#2D6A4F";
            case FREELANCE:     return "#52B788";
            case INVESTMENT:    return "#1B4332";
            case GIFT:          return "#D4A373";
            default:            return "#888888";
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

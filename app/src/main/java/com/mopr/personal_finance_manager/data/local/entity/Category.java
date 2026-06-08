package com.mopr.personal_finance_manager.data.local.entity;

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

    // Vietnamese display names
    public static String getDisplayName(String key) {
        switch (key) {
            case FOOD:          return "Ăn uống";
            case TRANSPORT:     return "Di chuyển";
            case BILLS:         return "Hóa đơn";
            case SHOPPING:      return "Mua sắm";
            case HEALTH:        return "Sức khỏe";
            case ENTERTAINMENT: return "Giải trí";
            case OTHER:         return "Khác";
            case SALARY:        return "Lương";
            case FREELANCE:     return "Freelance";
            case INVESTMENT:    return "Đầu tư";
            case GIFT:          return "Quà tặng";
            case OTHER_INCOME:  return "Thu nhập khác";
            default:            return key;
        }
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

package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * A budget entry: one spending limit for one category in one period.
 * <p>
 * period type:  "MONTH" | "WEEK" | "DAY"
 * periodKey:
 * MONTH → "yyyy-MM"            e.g. "2026-06"
 * WEEK  → "yyyy-Www"           e.g. "2026-W24"
 * DAY   → "yyyy-MM-dd"         e.g. "2026-06-17"
 */
@Entity(tableName = "budgets")
public class Budget implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * Category key — matches Category constants (FOOD, TRANSPORT, etc.)
     */
    public String category;

    /**
     * The spending limit set by the user
     */
    public double limitAmount;

    /**
     * Period type: "MONTH", "WEEK", or "DAY".
     * Defaults to "MONTH" for backwards compatibility.
     */
    public String periodType;

    /**
     * Formatted period key — format depends on periodType.
     * Old rows that only had "month" will have periodType=null; treat as MONTH.
     */
    public String periodKey;

    /**
     * Firestore doc ID for cloud sync
     */
    public String firestoreId;

    public Budget() {
        this.periodType = "MONTH";
    }

    public Budget(String category, double limitAmount, String periodType, String periodKey) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.periodType = periodType != null ? periodType : "MONTH";
        this.periodKey = periodKey;
    }

    // ── Convenience factory methods ──────────────────────────────────

    public static Budget forMonth(String category, double limit, String monthKey) {
        return new Budget(category, limit, "MONTH", monthKey);
    }

    public static Budget forWeek(String category, double limit, String weekKey) {
        return new Budget(category, limit, "WEEK", weekKey);
    }

    public static Budget forDay(String category, double limit, String dayKey) {
        return new Budget(category, limit, "DAY", dayKey);
    }

    // ── Legacy compat: old "month" column alias ──────────────────────

    /**
     * Returns the period key (backwards-compatible: used to be called "month").
     */
    public String getMonth() {
        return periodKey;
    }
}

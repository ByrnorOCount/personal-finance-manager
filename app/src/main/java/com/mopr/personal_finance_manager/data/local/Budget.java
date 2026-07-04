package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "budgets")
public class Budget implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int categoryId;
    public String category; // Name of category (redundant but used in some DAOs)
    public String type; // INCOME or EXPENSE
    public double limitAmount;
    public long startDate;
    public long endDate;
    public String periodType; // e.g., "MONTH"
    public String periodKey;  // e.g., "2026-06"
    public String firestoreId;

    public Budget() {}

    public Budget(int categoryId, String type, double limitAmount, long startDate, long endDate) {
        this.categoryId = categoryId;
        this.type = type;
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Budget(String category, double limitAmount, String periodType, String periodKey) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.periodType = periodType;
        this.periodKey = periodKey;
    }
}

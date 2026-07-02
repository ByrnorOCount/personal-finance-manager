package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "budgets")
public class Budget implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int categoryId;
    public String type; // INCOME or EXPENSE
    public double limitAmount;
    public long startDate;
    public long endDate;

    public Budget() {}

    public Budget(int categoryId, String type, double limitAmount, long startDate, long endDate) {
        this.categoryId = categoryId;
        this.type = type;
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

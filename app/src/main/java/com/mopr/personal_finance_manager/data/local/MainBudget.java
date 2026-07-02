package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "main_budgets")
public class MainBudget implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public long startDate;
    public long endDate;
    public double initialBalance;
    public boolean isActive;

    public MainBudget() {}

    public MainBudget(String name, long startDate, long endDate, double initialBalance, boolean isActive) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialBalance = initialBalance;
        this.isActive = isActive;
    }
}

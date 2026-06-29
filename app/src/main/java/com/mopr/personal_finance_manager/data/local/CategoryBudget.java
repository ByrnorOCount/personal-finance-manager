package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "category_budgets")
public class CategoryBudget implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int mainBudgetId;
    public String category;
    public double limitAmount;
    public String type; // INCOME or EXPENSE

    public CategoryBudget() {}

    public CategoryBudget(int mainBudgetId, String category, double limitAmount, String type) {
        this.mainBudgetId = mainBudgetId;
        this.category = category;
        this.limitAmount = limitAmount;
        this.type = type;
    }
}

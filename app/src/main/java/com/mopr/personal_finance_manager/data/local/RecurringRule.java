package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "recurring_rules")
public class RecurringRule implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String type; // INCOME or EXPENSE
    public double amount;
    public int categoryId;
    public String frequency; // DAILY, WEEKLY, MONTHLY
    public long nextOccurrence;
    public long lastGenerated;
    public boolean isActive;

    public RecurringRule() {}
}

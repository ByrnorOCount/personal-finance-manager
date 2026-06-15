package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "budgets")
public class Budget implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Same category keys as Transaction: FOOD, TRANSPORT, etc.
    public String category;

    // The spending limit set by user
    public double limitAmount;

    // Month this budget applies to: stored as "YYYY-MM" e.g. "2025-06"
    public String month;

    // Firestore doc ID
    public String firestoreId;

    public Budget() {
    }

    public Budget(String category, double limitAmount, String month) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.month = month;
    }
}

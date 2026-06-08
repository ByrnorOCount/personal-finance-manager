package com.mopr.personal_finance_manager.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "transactions")
public class Transaction implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // INCOME or EXPENSE
    public String type;

    public double amount;

    // Category key: FOOD, TRANSPORT, BILLS, SHOPPING, HEALTH, ENTERTAINMENT, OTHER
    public String category;

    // Date stored as epoch milliseconds for easy sorting and filtering
    public long date;

    // Optional note from user
    public String note;

    // Currency: VND or USD
    public String currency;

    // Firestore doc ID for cloud sync
    public String firestoreId;

    public Transaction() {}

    public Transaction(String type, double amount, String category,
                       long date, String note, String currency) {
        this.type     = type;
        this.amount   = amount;
        this.category = category;
        this.date     = date;
        this.note     = note;
        this.currency = currency;
    }

    // Convenience
    public boolean isIncome()  { return "INCOME".equals(type); }
    public boolean isExpense() { return "EXPENSE".equals(type); }
}

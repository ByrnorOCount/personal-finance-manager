package com.mopr.personal_finance_manager.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "savings_goals")
public class SavingsGoal implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;           // e.g. "New Phone", "Vacation"
    public double targetAmount;   // amount to reach
    public double savedAmount;    // amount contributed so far
    public long deadline;         // epoch ms — when to reach the goal by
    public boolean isCompleted;
    public String note;

    // Firestore doc ID
    public String firestoreId;

    public SavingsGoal() {
    }

    public SavingsGoal(String name, double targetAmount, double savedAmount,
                       long deadline, String note) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.note = note;
        this.isCompleted = false;
    }

    // Progress 0.0 to 1.0
    public float getProgress() {
        if (targetAmount <= 0) return 0f;
        return (float) Math.min(savedAmount / targetAmount, 1.0);
    }

    public int getProgressPercent() {
        return (int) (getProgress() * 100);
    }
}

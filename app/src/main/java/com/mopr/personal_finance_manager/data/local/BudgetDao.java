package com.mopr.personal_finance_manager.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Query("DELETE FROM budgets WHERE id = :id")
    void deleteById(int id);

    // All budgets for a given month (e.g. "2025-06")
    @Query("SELECT * FROM budgets WHERE month = :month")
    LiveData<List<Budget>> getBudgetsForMonth(String month);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE month = :month")
    LiveData<Double> getTotalBudgetedForMonth(String month);

    // Specific category + month — for checking if one already exists
    @Query("SELECT * FROM budgets WHERE category = :category AND month = :month LIMIT 1")
    LiveData<Budget> getBudgetForCategoryAndMonth(String category, String month);

    // Copy budgets to next month — called from BudgetPlannerActivity
    // Returns all budgets from source month so we can re-insert with new month
    @Query("SELECT * FROM budgets WHERE month = :sourceMonth")
    List<Budget> getBudgetsToClone(String sourceMonth);

    @Query("DELETE FROM budgets")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Budget> budgets);
}

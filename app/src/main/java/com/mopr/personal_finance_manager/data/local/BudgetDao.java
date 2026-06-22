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

    // ── Period-aware queries ──────────────────────────────────────────

    /**
     * All budgets for a given period type + key, e.g. ("MONTH", "2026-06")
     */
    @Query("SELECT * FROM budgets WHERE periodType = :type AND periodKey = :key ORDER BY category ASC")
    LiveData<List<Budget>> getBudgetsForPeriod(String type, String key);

    /**
     * Sum of all budget limits for a period
     */
    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE periodType = :type AND periodKey = :key")
    LiveData<Double> getTotalBudgetedForPeriod(String type, String key);

    /**
     * Check if a budget already exists for this category + period
     */
    @Query("SELECT * FROM budgets WHERE category = :category AND periodType = :type AND periodKey = :key LIMIT 1")
    Budget getBudgetForCategoryAndPeriodSync(String category, String type, String key);

    // ── Legacy month-based queries (keep for backwards compat) ───────

    @Query("SELECT * FROM budgets WHERE periodKey = :month ORDER BY category ASC")
    LiveData<List<Budget>> getBudgetsForMonth(String month);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE periodKey = :month")
    LiveData<Double> getTotalBudgetedForMonth(String month);

    // ── Bulk operations ──────────────────────────────────────────────

    /**
     * Fetch budgets to clone to next period
     */
    @Query("SELECT * FROM budgets WHERE periodType = :type AND periodKey = :key")
    List<Budget> getBudgetsToClone(String type, String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Budget> budgets);

    @Query("DELETE FROM budgets")
    void deleteAll();
}

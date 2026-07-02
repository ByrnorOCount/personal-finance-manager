package com.mopr.personal_finance_manager.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
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

    // ── Date range queries ───────────────────────────────────────────

    @Query("SELECT * FROM budgets WHERE type = :type AND NOT (endDate < :start OR startDate > :end)")
    LiveData<List<Budget>> getBudgetsInRange(String type, long start, long end);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE type = :type AND NOT (endDate < :start OR startDate > :end)")
    LiveData<Double> getTotalBudgetedInRange(String type, long start, long end);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND startDate = :start AND endDate = :end LIMIT 1")
    Budget getBudgetForCategoryAndPeriodSync(int categoryId, long start, long end);

    // ── Period Key queries ────────────────────────────────────────────

    @Query("SELECT * FROM budgets WHERE periodType = :type AND periodKey = :key ORDER BY category ASC")
    LiveData<List<Budget>> getBudgetsForPeriod(String type, String key);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE periodType = :type AND periodKey = :key")
    LiveData<Double> getTotalBudgetedForPeriod(String type, String key);

    @Query("SELECT * FROM budgets WHERE category = :category AND periodType = :type AND periodKey = :key LIMIT 1")
    Budget getBudgetForCategoryAndPeriodSync(String category, String type, String key);

    // ── Legacy month-based queries ───────

    @Query("SELECT * FROM budgets WHERE periodKey = :month ORDER BY category ASC")
    LiveData<List<Budget>> getBudgetsForMonth(String month);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE periodKey = :month")
    LiveData<Double> getTotalBudgetedForMonth(String month);

    // ── Bulk operations ──────────────────────────────────────────────

    @Query("SELECT * FROM budgets WHERE periodType = :type AND periodKey = :key")
    List<Budget> getBudgetsToClone(String type, String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Budget> budgets);

    @Query("DELETE FROM budgets")
    void deleteAll();

    // ── NEW BUDGET SYSTEM ──────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMainBudget(MainBudget mainBudget);

    @Update
    void updateMainBudget(MainBudget mainBudget);

    @Query("SELECT * FROM main_budgets ORDER BY startDate DESC")
    LiveData<List<MainBudget>> getAllMainBudgets();

    @Query("SELECT * FROM main_budgets WHERE isActive = 1 LIMIT 1")
    LiveData<MainBudget> getActiveMainBudget();

    @Query("SELECT * FROM main_budgets WHERE id = :id")
    MainBudget getMainBudgetSync(int id);

    @Query("UPDATE main_budgets SET isActive = 0")
    void deactivateAllMainBudgets();

    @Query("UPDATE main_budgets SET isActive = 1 WHERE id = :id")
    void activateMainBudget(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategoryBudgets(List<CategoryBudget> categoryBudgets);

    @Query("SELECT * FROM category_budgets WHERE mainBudgetId = :mainBudgetId")
    LiveData<List<CategoryBudget>> getCategoryBudgetsForMainBudget(int mainBudgetId);

    @Query("SELECT * FROM category_budgets WHERE mainBudgetId = :mainBudgetId")
    List<CategoryBudget> getCategoryBudgetsForMainBudgetSync(int mainBudgetId);

    @Query("DELETE FROM main_budgets WHERE id = :id")
    void deleteMainBudgetById(int id);

    @Query("DELETE FROM category_budgets WHERE mainBudgetId = :mainBudgetId")
    void deleteCategoryBudgetsByMainBudgetId(int mainBudgetId);

    @Update
    void updateCategoryBudget(CategoryBudget categoryBudget);

    @Delete
    void deleteCategoryBudget(CategoryBudget categoryBudget);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategoryBudget(CategoryBudget categoryBudget);
}

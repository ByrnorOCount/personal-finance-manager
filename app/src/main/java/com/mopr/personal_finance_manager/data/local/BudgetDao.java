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

    @Query("SELECT * FROM budgets WHERE type = :type AND startDate <= :date AND endDate >= :date")
    LiveData<List<Budget>> getBudgetsForDate(String type, long date);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE type = :type AND startDate <= :date AND endDate >= :date")
    LiveData<Double> getTotalBudgetedForDate(String type, long date);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND startDate = :start AND endDate = :end LIMIT 1")
    Budget getBudgetForCategoryAndPeriodSync(int categoryId, long start, long end);

    @Query("SELECT * FROM budgets WHERE startDate = :start AND endDate = :end")
    List<Budget> getBudgetsForExactRangeSync(long start, long end);

    @Query("SELECT * FROM budgets WHERE startDate = :start AND endDate = :end")
    LiveData<List<Budget>> getBudgetsForExactRange(long start, long end);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE startDate = :start AND endDate = :end")
    LiveData<Double> getTotalBudgetedForExactRange(long start, long end);

    @Query("SELECT * FROM budgets WHERE type = :type AND NOT (endDate < :start OR startDate > :end)")
    LiveData<List<Budget>> getBudgetsInRange(String type, long start, long end);

    @Query("SELECT COALESCE(SUM(limitAmount), 0) FROM budgets WHERE type = :type AND NOT (endDate < :start OR startDate > :end)")
    LiveData<Double> getTotalBudgetedInRange(String type, long start, long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Budget> budgets);
}

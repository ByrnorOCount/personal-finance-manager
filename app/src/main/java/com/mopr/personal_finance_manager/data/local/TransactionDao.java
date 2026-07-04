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
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(int id);

    // ── Home screen: 5 most recent ───────────────────────────────────────────
    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 5")
    LiveData<List<TransactionWithCategory>> getRecent5WithCategory();

    // ── Home screen: total balance (income - expense) ────────────────────────
    @Query("SELECT COALESCE(SUM(CASE WHEN type='INCOME' THEN amount ELSE -amount END), 0) FROM transactions")
    LiveData<Double> getTotalBalance();

    // ── Home screen: total income this month ─────────────────────────────────
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='INCOME' AND date >= :startMs AND date <= :endMs")
    LiveData<Double> getTotalIncomeInRange(long startMs, long endMs);

    // ── Home screen: total expense this month ────────────────────────────────
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='EXPENSE' AND date >= :startMs AND date <= :endMs")
    LiveData<Double> getTotalExpenseInRange(long startMs, long endMs);

    // ── Transaction History: all, newest first ───────────────────────────────
    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<TransactionWithCategory>> getAllWithCategory();

    // ── Transaction History: filter by type ─────────────────────────────────
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    List<Transaction> getByType(String type);

    // ── Transaction History: filter by category ──────────────────────────────
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    List<Transaction> getByCategory(int categoryId);

    // ── Transaction History: filter by date range ────────────────────────────
    @Query("SELECT * FROM transactions WHERE date >= :startMs AND date <= :endMs ORDER BY date DESC")
    List<Transaction> getInRange(long startMs, long endMs);

    // ── Transaction History: search by note keyword ──────────────────────────
    @Query("SELECT * FROM transactions WHERE note LIKE '%' || :keyword || '%' ORDER BY date DESC")
    List<Transaction> searchByNote(String keyword);

    // ── Statistics: expense by category in a month ───────────────────────────
    @Query("SELECT * FROM transactions WHERE type='EXPENSE' AND date >= :startMs AND date <= :endMs ORDER BY date DESC")
    List<Transaction> getExpensesInRange(long startMs, long endMs);

    @Query("SELECT COALESCE(c.parentId, c.id) as categoryId, " +
           "(SELECT name FROM categories WHERE id = COALESCE(c.parentId, c.id)) as category, " +
           "SUM(t.amount) as totalAmount " +
           "FROM transactions t INNER JOIN categories c ON t.categoryId = c.id " +
           "WHERE t.type='EXPENSE' AND t.date >= :startMs AND t.date <= :endMs " +
           "GROUP BY COALESCE(c.parentId, c.id)")
    LiveData<List<CategorySum>> getExpensesByCategoryInRange(long startMs, long endMs);

    @Query("SELECT COALESCE(c.parentId, c.id) as categoryId, " +
           "(SELECT name FROM categories WHERE id = COALESCE(c.parentId, c.id)) as category, " +
           "SUM(t.amount) as totalAmount " +
           "FROM transactions t INNER JOIN categories c ON t.categoryId = c.id " +
           "WHERE t.type='INCOME' AND t.date >= :startMs AND t.date <= :endMs " +
           "GROUP BY COALESCE(c.parentId, c.id)")
    LiveData<List<CategorySum>> getIncomeByCategoryInRange(long startMs, long endMs);

    @Query("SELECT t.categoryId, c.name as category, SUM(t.amount) as totalAmount " +
           "FROM transactions t INNER JOIN categories c ON t.categoryId = c.id " +
           "WHERE t.type='EXPENSE' AND t.date >= :startMs AND t.date <= :endMs GROUP BY t.categoryId")
    LiveData<List<CategorySum>> getRawExpensesByCategoryInRange(long startMs, long endMs);

    // ── Budget Planner: total spent per category in a month ──────────────────
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='EXPENSE' AND categoryId = :categoryId AND date >= :startMs AND date <= :endMs")
    double getExpenseByCategory(int categoryId, long startMs, long endMs);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='INCOME' AND categoryId = :categoryId AND date >= :startMs AND date <= :endMs")
    double getIncomeByCategory(int categoryId, long startMs, long endMs);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " +
           "INNER JOIN categories c ON t.categoryId = c.id " +
           "WHERE t.type='EXPENSE' AND (c.name = :categoryName OR " +
           "(SELECT name FROM categories WHERE id = c.parentId) = :categoryName) " +
           "AND t.date >= :startMs AND t.date <= :endMs")
    double getExpenseForCategoryNameGroupSync(String categoryName, long startMs, long endMs);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='EXPENSE' AND date >= :startMs AND date <= :endMs")
    double getTotalExpenseInRangeSync(long startMs, long endMs);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='INCOME' AND date >= :startMs AND date <= :endMs")
    double getTotalIncomeInRangeSync(long startMs, long endMs);

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE type='EXPENSE' AND date >= :startMs AND date <= :endMs")
    List<TransactionWithCategory> getExpensesWithCategoryInRangeSync(long startMs, long endMs);

    @Query("SELECT COALESCE(AVG(amount), 0) FROM transactions WHERE type='EXPENSE'")
    double getAverageExpenseAmountSync();

    // ── Settings: delete all data ────────────────────────────────────────────
    @Query("DELETE FROM transactions")
    void deleteAll();

    // ── Firestore sync ───────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Transaction> transactions);
}

package com.mopr.personal_finance_manager.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mopr.personal_finance_manager.data.local.entity.Transaction;

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
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 5")
    LiveData<List<Transaction>> getRecent5();

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
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAll();

    // ── Transaction History: filter by type ─────────────────────────────────
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    List<Transaction> getByType(String type);

    // ── Transaction History: filter by category ──────────────────────────────
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    List<Transaction> getByCategory(String category);

    // ── Transaction History: filter by date range ────────────────────────────
    @Query("SELECT * FROM transactions WHERE date >= :startMs AND date <= :endMs ORDER BY date DESC")
    List<Transaction> getInRange(long startMs, long endMs);

    // ── Transaction History: search by note keyword ──────────────────────────
    @Query("SELECT * FROM transactions WHERE note LIKE '%' || :keyword || '%' ORDER BY date DESC")
    List<Transaction> searchByNote(String keyword);

    // ── Statistics: expense by category in a month ───────────────────────────
    @Query("SELECT * FROM transactions WHERE type='EXPENSE' AND date >= :startMs AND date <= :endMs ORDER BY date DESC")
    List<Transaction> getExpensesInRange(long startMs, long endMs);

    // ── Budget Planner: total spent per category in a month ──────────────────
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='EXPENSE' AND category = :category AND date >= :startMs AND date <= :endMs")
    double getExpenseByCategory(String category, long startMs, long endMs);

    // ── Settings: delete all data ────────────────────────────────────────────
    @Query("DELETE FROM transactions")
    void deleteAll();

    // ── Firestore sync ───────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Transaction> transactions);
}

package com.mopr.personal_finance_manager.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.mopr.personal_finance_manager.data.local.AppDatabase;
import com.mopr.personal_finance_manager.data.local.dao.BudgetDao;
import com.mopr.personal_finance_manager.data.local.dao.SavingsGoalDao;
import com.mopr.personal_finance_manager.data.local.dao.TransactionDao;
import com.mopr.personal_finance_manager.data.local.entity.Budget;
import com.mopr.personal_finance_manager.data.local.entity.SavingsGoal;
import com.mopr.personal_finance_manager.data.local.entity.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinanceRepository {

    private static volatile FinanceRepository instance;
    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    private final SavingsGoalDao savingsGoalDao;
    private final ExecutorService executor;

    private FinanceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        savingsGoalDao = db.savingsGoalDao();
        executor = Executors.newFixedThreadPool(4);
    }

    public static FinanceRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (FinanceRepository.class) {
                if (instance == null) {
                    instance = new FinanceRepository(application);
                }
            }
        }
        return instance;
    }

    // ── Transactions ──────────────────────────────────────────────────────

    public void insertTransaction(Transaction t) {
        executor.execute(() -> transactionDao.insert(t));
    }

    public void updateTransaction(Transaction t) {
        executor.execute(() -> transactionDao.update(t));
    }

    public void deleteTransaction(Transaction t) {
        executor.execute(() -> transactionDao.delete(t));
    }

    public LiveData<List<Transaction>> getRecentTransactions() {
        return transactionDao.getRecent5();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAll();
    }

    public LiveData<Double> getTotalBalance() {
        return transactionDao.getTotalBalance();
    }

    public LiveData<Double> getTotalIncome(long start, long end) {
        return transactionDao.getTotalIncomeInRange(start, end);
    }

    public LiveData<Double> getTotalExpense(long start, long end) {
        return transactionDao.getTotalExpenseInRange(start, end);
    }

    // ── Budgets ───────────────────────────────────────────────────────────

    public void insertBudget(Budget budget) {
        executor.execute(() -> budgetDao.insert(budget));
    }

    public void updateBudget(Budget budget) {
        executor.execute(() -> budgetDao.update(budget));
    }

    public LiveData<List<Budget>> getBudgetsForMonth(String month) {
        return budgetDao.getBudgetsForMonth(month);
    }

    // ── Savings Goals ─────────────────────────────────────────────────────

    public void insertSavingsGoal(SavingsGoal goal) {
        executor.execute(() -> savingsGoalDao.insert(goal));
    }

    public void updateSavingsGoal(SavingsGoal goal) {
        executor.execute(() -> savingsGoalDao.update(goal));
    }

    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return savingsGoalDao.getActive();
    }
}

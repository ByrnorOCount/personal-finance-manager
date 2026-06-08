package com.mopr.personal_finance_manager.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    private final SavingsGoalDao savingsGoalDao;
    private final ExecutorService executorService;

    private static FinanceRepository instance;

    private FinanceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        savingsGoalDao = db.savingsGoalDao();
        executorService = Executors.newFixedThreadPool(4);
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

    // Transactions
    public void insertTransaction(Transaction transaction) {
        executorService.execute(() -> transactionDao.insert(transaction));
    }

    public void updateTransaction(Transaction transaction) {
        executorService.execute(() -> transactionDao.update(transaction));
    }

    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> transactionDao.delete(transaction));
    }

    // Budgets
    public void insertBudget(Budget budget) {
        executorService.execute(() -> budgetDao.insert(budget));
    }

    public void updateBudget(Budget budget) {
        executorService.execute(() -> budgetDao.update(budget));
    }

    // Savings Goals
    public void insertSavingsGoal(SavingsGoal goal) {
        executorService.execute(() -> savingsGoalDao.insert(goal));
    }

    public void updateSavingsGoal(SavingsGoal goal) {
        executorService.execute(() -> savingsGoalDao.update(goal));
    }

    // LiveData Getters
    public LiveData<List<Transaction>> getRecentTransactions() {
        return transactionDao.getRecent5();
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

    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAll();
    }

    public LiveData<List<Budget>> getBudgetsForMonth(String month) {
        return budgetDao.getBudgetsForMonth(month);
    }

    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return savingsGoalDao.getActive();
    }
}

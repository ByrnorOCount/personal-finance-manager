package com.mopr.personal_finance_manager.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.SavingsGoal;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.repository.FinanceRepository;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {

    private final FinanceRepository repository;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repository = FinanceRepository.getInstance(application);
    }

    // ── Home ──────────────────────────────────────────────────────────────

    public LiveData<List<Transaction>> getRecentTransactions() {
        return repository.getRecentTransactions();
    }

    public LiveData<Double> getTotalBalance() {
        return repository.getTotalBalance();
    }

    public LiveData<Double> getTotalIncome(long start, long end) {
        return repository.getTotalIncome(start, end);
    }

    public LiveData<Double> getTotalExpense(long start, long end) {
        return repository.getTotalExpense(start, end);
    }

    // ── History ───────────────────────────────────────────────────────────

    /**
     * Full transaction list, newest first — used by HistoryFragment.
     */
    public LiveData<List<Transaction>> getAllTransactions() {
        return repository.getAllTransactions();
    }

    // ── Budget ────────────────────────────────────────────────────────────

    public LiveData<List<Budget>> getBudgetsForMonth(String month) {
        return repository.getBudgetsForMonth(month);
    }

    // ── Savings ───────────────────────────────────────────────────────────

    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return repository.getActiveSavingsGoals();
    }

    // ── Mutations ─────────────────────────────────────────────────────────

    public void insertTransaction(Transaction transaction) {
        repository.insertTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        repository.updateTransaction(transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        repository.deleteTransaction(transaction);
    }

    public void insertBudget(Budget budget) {
        repository.insertBudget(budget);
    }

    public void insertSavingsGoal(SavingsGoal goal) {
        repository.insertSavingsGoal(goal);
    }

    public void updateSavingsGoal(SavingsGoal goal) {
        repository.updateSavingsGoal(goal);
    }
}

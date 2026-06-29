package com.mopr.personal_finance_manager.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.CategoryBudget;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.data.local.SavingsGoal;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.repository.FinanceRepository;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {

    private final FinanceRepository repo;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repo = FinanceRepository.getInstance(application);
    }

    // ── Transactions ──────────────────────────────────────────────────

    public LiveData<List<Transaction>> getRecentTransactions() {
        return repo.getRecentTransactions();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return repo.getAllTransactions();
    }

    public LiveData<Double> getTotalBalance() {
        return repo.getTotalBalance();
    }

    public LiveData<Double> getTotalIncome(long start, long end) {
        return repo.getTotalIncome(start, end);
    }

    public LiveData<Double> getTotalExpense(long start, long end) {
        return repo.getTotalExpense(start, end);
    }

    public LiveData<List<CategorySum>> getExpensesByCategory(long start, long end) {
        return repo.getExpensesByCategory(start, end);
    }

    public void insertTransaction(Transaction t) {
        repo.insertTransaction(t);
    }

    public void updateTransaction(Transaction t) {
        repo.updateTransaction(t);
    }

    public void deleteTransaction(Transaction t) {
        repo.deleteTransaction(t);
    }

    // ── Budgets ───────────────────────────────────────────────────────

    /**
     * Insert or silently update if same category+period already exists
     */
    public void upsertBudget(Budget b) {
        repo.upsertBudget(b);
    }

    public void updateBudget(Budget b) {
        repo.updateBudget(b);
    }

    public void deleteBudget(int id) {
        repo.deleteBudget(id);
    }

    public LiveData<List<Budget>> getBudgetsForPeriod(String type, String key) {
        return repo.getBudgetsForPeriod(type, key);
    }

    public LiveData<Double> getTotalBudgetedForPeriod(String type, String key) {
        return repo.getTotalBudgetedForPeriod(type, key);
    }

    public void cloneBudgets(String periodType, String fromKey, String toKey) {
        repo.cloneBudgets(periodType, fromKey, toKey);
    }

    // Legacy month helpers used by HomeFragment
    public LiveData<List<Budget>> getBudgetsForMonth(String month) {
        return repo.getBudgetsForMonth(month);
    }

    public LiveData<Double> getTotalBudgetedForMonth(String month) {
        return repo.getTotalBudgetedForMonth(month);
    }

    // ── NEW BUDGET SYSTEM ──────────────────────────────────────────

    public void insertMainBudget(MainBudget mb, List<CategoryBudget> cbs) {
        repo.insertMainBudget(mb, cbs);
    }

    public void updateMainBudget(MainBudget mb) {
        repo.updateMainBudget(mb);
    }

    public void activateMainBudget(int id) {
        repo.activateMainBudget(id);
    }

    public LiveData<List<MainBudget>> getAllMainBudgets() {
        return repo.getAllMainBudgets();
    }

    public LiveData<MainBudget> getActiveMainBudget() {
        return repo.getActiveMainBudget();
    }

    public LiveData<List<CategoryBudget>> getCategoryBudgetsForMainBudget(int mainBudgetId) {
        return repo.getCategoryBudgetsForMainBudget(mainBudgetId);
    }

    public void deleteMainBudget(int id) {
        repo.deleteMainBudget(id);
    }

    // ── Savings ───────────────────────────────────────────────────────

    public void insertSavingsGoal(SavingsGoal g) {
        repo.insertSavingsGoal(g);
    }

    public void updateSavingsGoal(SavingsGoal g) {
        repo.updateSavingsGoal(g);
    }

    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return repo.getActiveSavingsGoals();
    }

    public LiveData<List<SavingsGoal>> getAllSavingsGoals() {
        return repo.getAllSavingsGoals();
    }
}

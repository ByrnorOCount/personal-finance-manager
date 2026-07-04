package com.mopr.personal_finance_manager.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.data.local.CategoryBudget;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.data.local.SavingsGoal;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.local.TransactionWithCategory;
import com.mopr.personal_finance_manager.data.repository.FinanceRepository;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {

    private final FinanceRepository repo;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repo = FinanceRepository.getInstance(application);
    }

    // ── Transactions ──────────────────────────────────────────────────

    public LiveData<List<TransactionWithCategory>> getRecentTransactions() {
        return repo.getRecentTransactions();
    }

    public LiveData<List<TransactionWithCategory>> getAllTransactions() {
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

    public LiveData<List<CategorySum>> getIncomeByCategoryInRange(long start, long end) {
        return repo.getIncomeByCategoryInRange(start, end);
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

    // ── Categories ────────────────────────────────────────────────────

    public LiveData<List<Category>> getAllCategories() {
        return repo.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(String type) {
        return repo.getCategoriesByType(type);
    }

    public void insertCategory(Category category) {
        repo.insertCategory(category);
    }

    public void ensureCategoryExists(String name, String type) {
        repo.ensureCategoryExists(name, type);
    }

    public void updateCategory(Category category) {
        repo.updateCategory(category);
    }

    public void deleteCategory(int id) {
        repo.deleteCategory(id);
    }

    // ── Budgets ───────────────────────────────────────────────────────

    public void upsertBudget(Budget b) {
        repo.upsertBudget(b);
    }

    public void updateBudget(Budget b) {
        repo.updateBudget(b);
    }

    public void deleteBudget(int id) {
        repo.deleteBudget(id);
    }

    public LiveData<List<Budget>> getBudgetsInRange(String type, long start, long end) {
        return repo.getBudgetsInRange(type, start, end);
    }

    public LiveData<Double> getTotalBudgetedInRange(String type, long start, long end) {
        return repo.getTotalBudgetedInRange(type, start, end);
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

    public void updateCategoryBudget(CategoryBudget cb) {
        repo.updateCategoryBudget(cb);
    }

    public void deleteCategoryBudget(CategoryBudget cb) {
        repo.deleteCategoryBudget(cb);
    }

    public void insertCategoryBudget(CategoryBudget cb) {
        repo.insertCategoryBudget(cb);
    }

    public void clearAllData() {
        repo.clearAllData();
    }

    public void generateRandomBudget() {
        repo.generateRandomBudget();
    }

    // ── Savings ───────────────────────────────────────────────────────

    public void insertSavingsGoal(SavingsGoal g) {
        repo.insertSavingsGoal(g);
    }

    public void updateSavingsGoal(SavingsGoal g) {
        repo.updateSavingsGoal(g);
    }

    public void deleteSavingsGoal(int id) {
        repo.deleteSavingsGoal(id);
    }

    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return repo.getActiveSavingsGoals();
    }

    public LiveData<List<SavingsGoal>> getAllSavingsGoals() {
        return repo.getAllSavingsGoals();
    }
}

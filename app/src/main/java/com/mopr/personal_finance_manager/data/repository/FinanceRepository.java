package com.mopr.personal_finance_manager.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.mopr.personal_finance_manager.data.local.AppDatabase;
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.BudgetDao;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.data.local.CategoryBudget;
import com.mopr.personal_finance_manager.data.local.CategoryDao;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.data.local.SavingsGoal;
import com.mopr.personal_finance_manager.data.local.SavingsGoalDao;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.local.TransactionDao;
import com.mopr.personal_finance_manager.data.local.TransactionWithCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinanceRepository {

    private static volatile FinanceRepository instance;
    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    private final SavingsGoalDao savingsGoalDao;
    private final CategoryDao categoryDao;
    private final ExecutorService executor;

    private FinanceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        savingsGoalDao = db.savingsGoalDao();
        categoryDao = db.categoryDao();
        executor = Executors.newFixedThreadPool(4);
    }

    public static FinanceRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (FinanceRepository.class) {
                if (instance == null) instance = new FinanceRepository(application);
            }
        }
        return instance;
    }

    // ── Transactions ──────────────────────────────────────────────────

    public void insertTransaction(Transaction t) {
        executor.execute(() -> transactionDao.insert(t));
    }

    public void updateTransaction(Transaction t) {
        executor.execute(() -> transactionDao.update(t));
    }

    public void deleteTransaction(Transaction t) {
        executor.execute(() -> transactionDao.delete(t));
    }

    public LiveData<List<TransactionWithCategory>> getRecentTransactions() {
        return transactionDao.getRecent5WithCategory();
    }

    public LiveData<List<TransactionWithCategory>> getAllTransactions() {
        return transactionDao.getAllWithCategory();
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

    public LiveData<List<CategorySum>> getExpensesByCategory(long start, long end) {
        return transactionDao.getExpensesByCategoryInRange(start, end);
    }

    public LiveData<List<CategorySum>> getIncomeByCategoryInRange(long start, long end) {
        return transactionDao.getIncomeByCategoryInRange(start, end);
    }

    // ── Categories ────────────────────────────────────────────────────

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(String type) {
        return categoryDao.getCategoriesByType(type);
    }

    public void insertCategory(Category category) {
        executor.execute(() -> categoryDao.insert(category));
    }

    public void updateCategory(Category category) {
        executor.execute(() -> categoryDao.update(category));
    }

    public void deleteCategory(int id) {
        executor.execute(() -> categoryDao.deleteById(id));
    }

    // ── Budgets — period-aware ────────────────────────────────────────

    public void insertBudget(Budget budget) {
        executor.execute(() -> budgetDao.insert(budget));
    }

    public void updateBudget(Budget budget) {
        executor.execute(() -> budgetDao.update(budget));
    }

    public void deleteBudget(int id) {
        executor.execute(() -> budgetDao.deleteById(id));
    }

    public void upsertBudget(Budget budget) {
        executor.execute(() -> {
            Budget existing;
            if (budget.periodKey != null) {
                existing = budgetDao.getBudgetForCategoryAndPeriodSync(
                    budget.category, budget.periodType, budget.periodKey);
            } else {
                existing = budgetDao.getBudgetForCategoryAndPeriodSync(
                    budget.categoryId, budget.startDate, budget.endDate);
            }

            if (existing != null) {
                existing.limitAmount = budget.limitAmount;
                budgetDao.update(existing);
            } else {
                budgetDao.insert(budget);
            }
        });
    }

    public LiveData<List<Budget>> getBudgetsInRange(String type, long start, long end) {
        return budgetDao.getBudgetsInRange(type, start, end);
    }

    public LiveData<Double> getTotalBudgetedInRange(String type, long start, long end) {
        return budgetDao.getTotalBudgetedInRange(type, start, end);
    }

    public LiveData<List<Budget>> getBudgetsForPeriod(String type, String key) {
        return budgetDao.getBudgetsForPeriod(type, key);
    }

    public LiveData<Double> getTotalBudgetedForPeriod(String type, String key) {
        return budgetDao.getTotalBudgetedForPeriod(type, key);
    }

    public void cloneBudgets(String periodType, String fromKey, String toKey) {
        executor.execute(() -> {
            List<Budget> source = budgetDao.getBudgetsToClone(periodType, fromKey);
            List<Budget> clones = new ArrayList<>();
            for (Budget b : source) {
                Budget clone = new Budget(b.category, b.limitAmount, periodType, toKey);
                clones.add(clone);
            }
            if (!clones.isEmpty()) budgetDao.insertAll(clones);
        });
    }

    public LiveData<List<Budget>> getBudgetsForMonth(String month) {
        return budgetDao.getBudgetsForMonth(month);
    }

    public LiveData<Double> getTotalBudgetedForMonth(String month) {
        return budgetDao.getTotalBudgetedForMonth(month);
    }

    // ── NEW BUDGET SYSTEM ──────────────────────────────────────────

    public void insertMainBudget(MainBudget mb, List<CategoryBudget> cbs) {
        executor.execute(() -> {
            if (mb.isActive) {
                budgetDao.deactivateAllMainBudgets();
            }
            long id = budgetDao.insertMainBudget(mb);
            for (CategoryBudget cb : cbs) {
                cb.mainBudgetId = (int) id;
                ensureCategoryExists(cb.category, cb.type);
            }
            budgetDao.insertCategoryBudgets(cbs);
        });
    }

    public void ensureCategoryExists(String name, String type) {
        executor.execute(() -> {
            Category existing = categoryDao.getByNameAndType(name, type);
            if (existing == null) {
                Category newCat = new Category(
                    name,
                    type,
                    com.mopr.personal_finance_manager.data.model.Category.getIconRes(name),
                    com.mopr.personal_finance_manager.data.model.Category.getColorRes(name),
                    false
                );
                categoryDao.insert(newCat);
            }
        });
    }

    public void updateMainBudget(MainBudget mb) {
        executor.execute(() -> budgetDao.updateMainBudget(mb));
    }

    public void activateMainBudget(int id) {
        executor.execute(() -> {
            budgetDao.deactivateAllMainBudgets();
            budgetDao.activateMainBudget(id);
        });
    }

    public LiveData<List<MainBudget>> getAllMainBudgets() {
        return budgetDao.getAllMainBudgets();
    }

    public LiveData<MainBudget> getActiveMainBudget() {
        return budgetDao.getActiveMainBudget();
    }

    public LiveData<List<CategoryBudget>> getCategoryBudgetsForMainBudget(int mainBudgetId) {
        return budgetDao.getCategoryBudgetsForMainBudget(mainBudgetId);
    }

    public void deleteMainBudget(int id) {
        executor.execute(() -> {
            budgetDao.deleteMainBudgetById(id);
            budgetDao.deleteCategoryBudgetsByMainBudgetId(id);
        });
    }

    public void updateCategoryBudget(CategoryBudget cb) {
        executor.execute(() -> budgetDao.updateCategoryBudget(cb));
    }

    public void deleteCategoryBudget(CategoryBudget cb) {
        executor.execute(() -> budgetDao.deleteCategoryBudget(cb));
    }

    public void insertCategoryBudget(CategoryBudget cb) {
        executor.execute(() -> {
            ensureCategoryExists(cb.category, cb.type);
            budgetDao.insertCategoryBudget(cb);
        });
    }

    // ── Savings Goals ─────────────────────────────────────────────────

    public void insertSavingsGoal(SavingsGoal goal) {
        executor.execute(() -> savingsGoalDao.insert(goal));
    }

    public void updateSavingsGoal(SavingsGoal goal) {
        executor.execute(() -> savingsGoalDao.update(goal));
    }

    public void deleteSavingsGoal(int id) {
        executor.execute(() -> savingsGoalDao.deleteById(id));
    }

    public LiveData<List<SavingsGoal>> getActiveSavingsGoals() {
        return savingsGoalDao.getActive();
    }

    public LiveData<List<SavingsGoal>> getAllSavingsGoals() {
        return savingsGoalDao.getAll();
    }
}

package com.mopr.personal_finance_manager.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.mopr.personal_finance_manager.data.local.AppDatabase;
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.BudgetDao;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.data.local.CategoryDao;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.RecurringRule;
import com.mopr.personal_finance_manager.data.local.RecurringRuleDao;
import com.mopr.personal_finance_manager.data.local.SavingsGoal;
import com.mopr.personal_finance_manager.data.local.SavingsGoalDao;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.local.TransactionDao;
import com.mopr.personal_finance_manager.data.local.TransactionWithCategory;
import com.mopr.personal_finance_manager.data.logic.RecurringTransactionManager;
import com.mopr.personal_finance_manager.util.DateUtils;

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
    private final RecurringRuleDao recurringRuleDao;
    private final RecurringTransactionManager recurringManager;
    private final ExecutorService executor;

    private FinanceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        savingsGoalDao = db.savingsGoalDao();
        categoryDao = db.categoryDao();
        recurringRuleDao = db.recurringRuleDao();
        recurringManager = new RecurringTransactionManager(recurringRuleDao, transactionDao, categoryDao);
        executor = Executors.newFixedThreadPool(4);

        // Run recurring check on startup
        executor.execute(recurringManager::checkAndGenerateTransactions);
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

    // ── Recurring Rules ───────────────────────────────────────────────

    public LiveData<List<RecurringRule>> getAllRecurringRules() {
        return recurringRuleDao.getAllRules();
    }

    public void insertRecurringRule(RecurringRule rule) {
        executor.execute(() -> {
            recurringRuleDao.insert(rule);
            recurringManager.checkAndGenerateTransactions();
        });
    }

    public void updateRecurringRule(RecurringRule rule) {
        executor.execute(() -> {
            recurringRuleDao.update(rule);
            recurringManager.checkAndGenerateTransactions();
        });
    }

    public void deleteRecurringRule(RecurringRule rule) {
        executor.execute(() -> recurringRuleDao.delete(rule));
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

    /**
     * Insert or update: if a budget for the same category+period exists, update its limit.
     */
    public void upsertBudget(Budget budget) {
        executor.execute(() -> {
            Budget existing = budgetDao.getBudgetForCategoryAndPeriodSync(
                budget.categoryId, budget.startDate, budget.endDate);
            if (existing != null) {
                existing.limitAmount = budget.limitAmount;
                budgetDao.update(existing);
            } else {
                budgetDao.insert(budget);
            }
        });
    }

    public LiveData<List<Budget>> getBudgetsForDate(String type, long date) {
        return budgetDao.getBudgetsForDate(type, date);
    }

    public LiveData<Double> getTotalBudgetedForDate(String type, long date) {
        return budgetDao.getTotalBudgetedForDate(type, date);
    }

    public LiveData<List<Budget>> getBudgetsInRange(String type, long start, long end) {
        return budgetDao.getBudgetsInRange(type, start, end);
    }

    public LiveData<Double> getTotalBudgetedInRange(String type, long start, long end) {
        return budgetDao.getTotalBudgetedInRange(type, start, end);
    }

    /**
     * Copy all budgets from one period key to another (same type).
     */
    public void cloneBudgets(String periodType, String fromKey, String toKey) {
        executor.execute(() -> {
            long[] fromRange = DateUtils.getRangeForPeriod(periodType, fromKey);
            List<Budget> source = budgetDao.getBudgetsForExactRangeSync(fromRange[0], fromRange[1]);

            long[] toRange = DateUtils.getRangeForPeriod(periodType, toKey);
            List<Budget> clones = new ArrayList<>();
            for (Budget b : source) {
                Budget clone = new Budget(b.categoryId, b.type, b.limitAmount, toRange[0], toRange[1]);
                clones.add(clone);
            }
            if (!clones.isEmpty()) budgetDao.insertAll(clones);
        });
    }

    // ── Legacy month-based (keep HomeFragment working) ────────────────

    public LiveData<List<Budget>> getBudgetsForMonth(String month) {
        long[] range = DateUtils.getRangeForPeriod("MONTH", month);
        return budgetDao.getBudgetsForExactRange(range[0], range[1]);
    }

    public LiveData<Double> getTotalBudgetedForMonth(String month) {
        long[] range = DateUtils.getRangeForPeriod("MONTH", month);
        return budgetDao.getTotalBudgetedForExactRange(range[0], range[1]);
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

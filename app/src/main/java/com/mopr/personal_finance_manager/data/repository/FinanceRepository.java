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

    public void clearAllData() {
        executor.execute(() -> {
            transactionDao.deleteAll();
            budgetDao.deleteAll();
            budgetDao.deleteAllMainBudgets();
            budgetDao.deleteAllCategoryBudgets();
            savingsGoalDao.deleteAll();
            categoryDao.deleteAll();
        });
    }

    public void generateRandomBudget() {
        executor.execute(() -> {
            java.util.Random random = new java.util.Random();

            // 1. Setup Dates (Current Month)
            java.util.Calendar start = java.util.Calendar.getInstance();
            start.set(java.util.Calendar.DAY_OF_MONTH, 1);
            long startMs = com.mopr.personal_finance_manager.util.DateUtils.getStartOfDay(start);

            java.util.Calendar end = java.util.Calendar.getInstance();
            end.set(java.util.Calendar.DAY_OF_MONTH, end.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
            long endMs = com.mopr.personal_finance_manager.util.DateUtils.getEndOfDay(end);

            // Format name like "July 01-31, 2026"
            java.text.SimpleDateFormat monthFmt = new java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault());
            java.text.SimpleDateFormat dayFmt = new java.text.SimpleDateFormat("dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat yearFmt = new java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault());

            String budgetName = String.format("%s %s-%s, %s",
                monthFmt.format(start.getTime()),
                dayFmt.format(start.getTime()),
                dayFmt.format(end.getTime()),
                yearFmt.format(start.getTime()));

            // 2. Initial Balance (Random even VND, last 3 zeros)
            // Range 5M - 20M
            double initialBalance = (5000 + random.nextInt(15001)) * 1000.0;

            budgetDao.deactivateAllMainBudgets();
            MainBudget mainBudget = new MainBudget(budgetName, startMs, endMs, initialBalance, true);
            int mainBudgetId = (int) budgetDao.insertMainBudget(mainBudget);

            // 3. Categories and Budgets
            String[] incomeCats = {"Salary", "Freelance", "Investment", "Gift"};
            String[] expenseCats = {"Food", "Transport", "Bills", "Shopping", "Entertainment", "Health"};

            java.util.List<CategoryBudget> catBudgets = new java.util.ArrayList<>();

            // Incomes: ~20M to 50M total
            for (String catName : incomeCats) {
                double limit = (10000 + random.nextInt(20001)) * 1000.0;

                // Synchronous check/insert since we're already in a background thread
                Category existing = categoryDao.getByNameAndType(catName, "INCOME");
                if (existing == null) {
                    categoryDao.insert(new Category(catName, "INCOME",
                        com.mopr.personal_finance_manager.data.model.Category.getIconRes(catName),
                        com.mopr.personal_finance_manager.data.model.Category.getColorRes(catName), false));
                }

                catBudgets.add(new CategoryBudget(mainBudgetId, catName, limit, "INCOME"));
            }

            // Expenses: ~1M to 10M per category
            for (String catName : expenseCats) {
                double limit = (1000 + random.nextInt(9001)) * 1000.0;

                Category existing = categoryDao.getByNameAndType(catName, "EXPENSE");
                if (existing == null) {
                    categoryDao.insert(new Category(catName, "EXPENSE",
                        com.mopr.personal_finance_manager.data.model.Category.getIconRes(catName),
                        com.mopr.personal_finance_manager.data.model.Category.getColorRes(catName), false));
                }

                catBudgets.add(new CategoryBudget(mainBudgetId, catName, limit, "EXPENSE"));
            }
            budgetDao.insertCategoryBudgets(catBudgets);

            // 4. Random Transactions
            // For each category, add some transactions that don't exceed the limit
            for (CategoryBudget cb : catBudgets) {
                Category cat = categoryDao.getByNameAndType(cb.category, cb.type);
                if (cat == null) continue;

                int numTrans = 2 + random.nextInt(5);
                double totalUsed = 0;
                for (int i = 0; i < numTrans; i++) {
                    double remaining = cb.limitAmount - totalUsed;
                    if (remaining <= 1000) break;

                    double amount = (1 + random.nextInt((int)(remaining / 2000))) * 1000.0;
                    if (amount < 1000) amount = 1000;

                    totalUsed += amount;

                    long randomDate = startMs + (long)(random.nextDouble() * (endMs - startMs));
                    Transaction t = new Transaction(cb.type, amount, cat.id, randomDate, "Random generated " + cat.name, "VND");
                    transactionDao.insert(t);
                }
            }
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

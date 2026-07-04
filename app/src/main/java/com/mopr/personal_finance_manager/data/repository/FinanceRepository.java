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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinanceRepository {

    private static FinanceRepository instance;
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
            instance = new FinanceRepository(application);
        }
        return instance;
    }

    // ── Transactions ──────────────────────────────────────────────────

    public void insertTransaction(Transaction transaction) {
        executor.execute(() -> transactionDao.insert(transaction));
    }

    public void updateTransaction(Transaction transaction) {
        executor.execute(() -> transactionDao.update(transaction));
    }

    public void deleteTransaction(Transaction transaction) {
        executor.execute(() -> transactionDao.delete(transaction));
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

    public LiveData<List<CategorySum>> getRawExpensesByCategoryInRange(long start, long end) {
        return transactionDao.getRawExpensesByCategoryInRange(start, end);
    }

    // ── Categories ────────────────────────────────────────────────────

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(String type) {
        return categoryDao.getCategoriesByType(type);
    }

    public LiveData<List<Category>> getSubcategories(int parentId) {
        return categoryDao.getSubcategories(parentId);
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

    // ── Budgets (Individual) ──────────────────────────────────────────

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

    public void cloneBudgets(String fromKey, String toKey, String periodType) {
        executor.execute(() -> {
            List<Budget> toClone = budgetDao.getBudgetsToClone(periodType, fromKey);
            for (Budget b : toClone) {
                Budget newB = new Budget();
                newB.category = b.category;
                newB.limitAmount = b.limitAmount;
                newB.periodType = periodType;
                newB.periodKey = toKey;
                budgetDao.insert(newB);
            }
        });
    }

    public LiveData<List<Budget>> getBudgetsForMonth(String month) {
        return budgetDao.getBudgetsForMonth(month);
    }

    public LiveData<Double> getTotalBudgetedForMonth(String month) {
        return budgetDao.getTotalBudgetedForMonth(month);
    }

    // ── NEW BUDGET SYSTEM ──────────────────────────────────────────────

    public void insertMainBudget(MainBudget mainBudget, List<CategoryBudget> categoryBudgets) {
        executor.execute(() -> {
            budgetDao.deactivateAllMainBudgets();
            int mainBudgetId = (int) budgetDao.insertMainBudget(mainBudget);
            for (CategoryBudget cb : categoryBudgets) {
                cb.mainBudgetId = mainBudgetId;
                ensureCategoryExists(cb.category, cb.type);
            }
            budgetDao.insertCategoryBudgets(categoryBudgets);
        });
    }

    public void ensureCategoryExists(String name, String type) {
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
    }

    public void updateMainBudget(MainBudget mainBudget) {
        executor.execute(() -> budgetDao.updateMainBudget(mainBudget));
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
            // Range 500k - 2M
            double initialBalance = (500 + random.nextInt(1501)) * 1000.0;

            budgetDao.deactivateAllMainBudgets();
            MainBudget mainBudget = new MainBudget(budgetName, startMs, endMs, initialBalance, true);
            int mainBudgetId = (int) budgetDao.insertMainBudget(mainBudget);

            // 3. Categories and Budgets
            String[] incomeCats = {"Salary", "Freelance", "Investment", "Gift"};
            String[] expenseCats = {"Food", "Transport", "Bills", "Shopping", "Entertainment", "Health"};

            java.util.Map<String, String[]> subcatMap = new java.util.HashMap<>();
            subcatMap.put("Salary", new String[]{"Primary Job", "Bonus"});
            subcatMap.put("Freelance", new String[]{"Project Alpha", "Project Beta"});
            subcatMap.put("Investment", new String[]{"Stocks", "Crypto"});
            subcatMap.put("Gift", new String[]{"Birthday", "Holiday"});
            subcatMap.put("Food", new String[]{"Groceries", "Restaurants", "Snacks"});
            subcatMap.put("Transport", new String[]{"Taxi", "Fuel", "Maintenance"});
            subcatMap.put("Bills", new String[]{"Electricity", "Water", "Internet"});
            subcatMap.put("Shopping", new String[]{"Clothes", "Gadgets"});
            subcatMap.put("Entertainment", new String[]{"Movies", "Gaming"});
            subcatMap.put("Health", new String[]{"Pharmacy", "Checkup"});

            java.util.List<CategoryBudget> catBudgets = new java.util.ArrayList<>();

            // Incomes: 1M to 3M per category
            for (String catName : incomeCats) {
                double limit = (1000 + random.nextInt(2001)) * 1000.0;
                Category existing = categoryDao.getByNameAndType(catName, "INCOME");
                if (existing == null) {
                    categoryDao.insert(new Category(catName, "INCOME",
                        com.mopr.personal_finance_manager.data.model.Category.getIconRes(catName),
                        com.mopr.personal_finance_manager.data.model.Category.getColorRes(catName), false));
                }
                catBudgets.add(new CategoryBudget(mainBudgetId, catName, limit, "INCOME"));
            }

            // Expenses: 100k to 1M per category
            for (String catName : expenseCats) {
                double limit = (100 + random.nextInt(901)) * 1000.0;
                Category existing = categoryDao.getByNameAndType(catName, "EXPENSE");
                if (existing == null) {
                    categoryDao.insert(new Category(catName, "EXPENSE",
                        com.mopr.personal_finance_manager.data.model.Category.getIconRes(catName),
                        com.mopr.personal_finance_manager.data.model.Category.getColorRes(catName), false));
                }
                catBudgets.add(new CategoryBudget(mainBudgetId, catName, limit, "EXPENSE"));
            }
            budgetDao.insertCategoryBudgets(catBudgets);

            // 4. Random Transactions with Mandatory Subcategories
            for (CategoryBudget cb : catBudgets) {
                Category parentCat = categoryDao.getByNameAndType(cb.category, cb.type);
                if (parentCat == null) continue;

                String[] subNames = subcatMap.get(cb.category);
                if (subNames == null) subNames = new String[]{"Misc " + cb.category};

                int numTrans = 4 + random.nextInt(9); // Doubled transaction count
                double totalUsed = 0;
                for (int i = 0; i < numTrans; i++) {
                    double remaining = cb.limitAmount - totalUsed;
                    if (remaining <= 1000) break;

                    double maxPerTrans = remaining / (numTrans - i);
                    if (maxPerTrans < 1000) maxPerTrans = 1000;

                    double amount = (1 + random.nextInt((int)(maxPerTrans / 1000))) * 1000.0;
                    totalUsed += amount;

                    // Ensure subcategory exists
                    String subName = subNames[random.nextInt(subNames.length)];
                    Category subCat = categoryDao.getByNameAndType(subName, cb.type);
                    int subId;
                    if (subCat == null) {
                        subId = (int) categoryDao.insert(new Category(subName, cb.type, parentCat.iconRes, parentCat.colorRes, false, parentCat.id));
                    } else {
                        subId = subCat.id;
                    }

                    long randomDate = startMs + (long)(random.nextDouble() * (endMs - startMs));
                    Transaction t = new Transaction(cb.type, amount, subId, randomDate, "Random " + subName, "VND");
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

    // ── Analytics & Predictions ────────────────────────────────────────

    public LiveData<com.mopr.personal_finance_manager.data.model.PredictionResult> getBudgetPrediction(long startMs, long endMs) {
        androidx.lifecycle.MutableLiveData<com.mopr.personal_finance_manager.data.model.PredictionResult> result = new androidx.lifecycle.MutableLiveData<>();
        executor.execute(() -> {
            double currentSpend = transactionDao.getTotalExpenseInRangeSync(startMs, endMs);
            double budgetLimit = budgetDao.getTotalBudgetedInRangeSync("EXPENSE", startMs, endMs);
            result.postValue(com.mopr.personal_finance_manager.util.BudgetPredictor.analyzeCurrentPeriod(currentSpend, budgetLimit, startMs, endMs));
        });
        return result;
    }

    public LiveData<List<Double>> getHistoricalMonthlyExpenses(int months) {
        androidx.lifecycle.MutableLiveData<List<Double>> result = new androidx.lifecycle.MutableLiveData<>();
        executor.execute(() -> {
            List<Double> history = new java.util.ArrayList<>();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            for (int i = 0; i < months; i++) {
                long start = com.mopr.personal_finance_manager.util.DateUtils.getStartOfMonth(cal);
                long end = com.mopr.personal_finance_manager.util.DateUtils.getEndOfMonth(cal);
                history.add(0, transactionDao.getTotalExpenseInRangeSync(start, end));
                cal.add(java.util.Calendar.MONTH, -1);
            }
            result.postValue(history);
        });
        return result;
    }

    public LiveData<List<TransactionWithCategory>> getAnomalies(long startMs, long endMs) {
        androidx.lifecycle.MutableLiveData<List<TransactionWithCategory>> result = new androidx.lifecycle.MutableLiveData<>();
        executor.execute(() -> {
            // 1. Get all expenses in current range
            List<TransactionWithCategory> recent = transactionDao.getExpensesWithCategoryInRangeSync(startMs, endMs);

            // 2. Get overall average spend per transaction (historically)
            double overallAvg = transactionDao.getAverageExpenseAmountSync();

            // 3. Filter using predictor
            List<TransactionWithCategory> anomalies = new java.util.ArrayList<>();
            for (TransactionWithCategory t : recent) {
                if (t.transaction.amount > overallAvg * 2.5) {
                    anomalies.add(t);
                }
            }
            result.postValue(anomalies);
        });
        return result;
    }
}

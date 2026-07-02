package com.mopr.personal_finance_manager.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mopr.personal_finance_manager.R;

import java.util.concurrent.Executors;

@Database(
    entities = {Transaction.class, Budget.class, SavingsGoal.class, Category.class, RecurringRule.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Migration 1 → 2:
     * Budget table gains periodType (TEXT DEFAULT 'MONTH') and
     * renames the old "month" column to "periodKey".
     * <p>
     * SQLite doesn't support RENAME COLUMN before 3.25 (API 30+),
     * so we recreate the table.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // 1. Create new table
            db.execSQL("CREATE TABLE IF NOT EXISTS `budgets_new` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`category` TEXT," +
                "`limitAmount` REAL NOT NULL," +
                "`periodType` TEXT DEFAULT 'MONTH'," +
                "`periodKey` TEXT," +
                "`firestoreId` TEXT)");

            // 2. Copy old data; old "month" column → periodKey
            db.execSQL("INSERT INTO `budgets_new` (id, category, limitAmount, periodType, periodKey, firestoreId) " +
                "SELECT id, category, limitAmount, 'MONTH', month, firestoreId FROM `budgets`");

            // 3. Drop old, rename new
            db.execSQL("DROP TABLE `budgets`");
            db.execSQL("ALTER TABLE `budgets_new` RENAME TO `budgets`");
        }
    };
    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "personal_finance_manager_db"
                )
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadExecutor().execute(() -> populateInitialCategories(getInstance(context)));
                    }
                })
                .fallbackToDestructiveMigration()   // safety net for dev
                .build();
        }
        return instance;
    }

    private static void populateInitialCategories(AppDatabase db) {
        CategoryDao dao = db.categoryDao();

        // Expense categories
        dao.insert(new Category("Food", "EXPENSE", R.drawable.ic_cat_food, R.color.cat_food, true));
        dao.insert(new Category("Transport", "EXPENSE", R.drawable.ic_cat_transport, R.color.cat_transport, true));
        dao.insert(new Category("Bills", "EXPENSE", R.drawable.ic_cat_bills, R.color.cat_bills, true));
        dao.insert(new Category("Shopping", "EXPENSE", R.drawable.ic_cat_shopping, R.color.cat_shopping, true));
        dao.insert(new Category("Health", "EXPENSE", R.drawable.ic_cat_health, R.color.cat_health, true));
        dao.insert(new Category("Entertainment", "EXPENSE", R.drawable.ic_cat_entertainment, R.color.cat_entertainment, true));
        dao.insert(new Category("Other", "EXPENSE", R.drawable.ic_cat_other, R.color.cat_other, true));

        // Income categories
        dao.insert(new Category("Salary", "INCOME", R.drawable.ic_cat_salary, R.color.cat_food, true)); // Using food color as placeholder
        dao.insert(new Category("Freelance", "INCOME", R.drawable.ic_cat_freelance, R.color.cat_transport, true));
        dao.insert(new Category("Investment", "INCOME", R.drawable.ic_cat_investment, R.color.cat_bills, true));
        dao.insert(new Category("Gift", "INCOME", R.drawable.ic_cat_gift, R.color.cat_shopping, true));
    }

    public abstract TransactionDao transactionDao();

    public abstract BudgetDao budgetDao();

    public abstract SavingsGoalDao savingsGoalDao();

    public abstract CategoryDao categoryDao();

    public abstract RecurringRuleDao recurringRuleDao();
}

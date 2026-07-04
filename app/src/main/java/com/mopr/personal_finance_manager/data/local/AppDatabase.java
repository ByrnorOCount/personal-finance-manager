package com.mopr.personal_finance_manager.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
    entities = {
        Transaction.class,
        Budget.class,
        SavingsGoal.class,
        MainBudget.class,
        CategoryBudget.class,
        Category.class,
        RecurringRule.class
    },
    version = 5,          // bumped from 4 → 5: Added note to CategoryBudget
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Migration 1 → 2:
     * Budget table gains periodType (TEXT DEFAULT 'MONTH') and
     * renames the old "month" column to "periodKey".
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
                "SELECT id, category, limitAmount, 'MONTH', month, firestoreId FROM `budgets` ");

            // 3. Drop old, rename new
            db.execSQL("DROP TABLE `budgets` ");
            db.execSQL("ALTER TABLE `budgets_new` RENAME TO `budgets` ");
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
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()   // safety net for dev
                .build();
        }
        return instance;
    }

    public abstract TransactionDao transactionDao();
    public abstract BudgetDao budgetDao();
    public abstract SavingsGoalDao savingsGoalDao();
    public abstract CategoryDao categoryDao();
    public abstract RecurringRuleDao recurringRuleDao();
}

package com.mopr.personal_finance_manager.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mopr.personal_finance_manager.data.local.dao.BudgetDao;
import com.mopr.personal_finance_manager.data.local.dao.SavingsGoalDao;
import com.mopr.personal_finance_manager.data.local.dao.TransactionDao;
import com.mopr.personal_finance_manager.data.local.entity.Budget;
import com.mopr.personal_finance_manager.data.local.entity.SavingsGoal;
import com.mopr.personal_finance_manager.data.local.entity.Transaction;

@Database(
    entities = {Transaction.class, Budget.class, SavingsGoal.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "personal_finance_manager_db"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    public abstract TransactionDao transactionDao();

    public abstract BudgetDao budgetDao();

    public abstract SavingsGoalDao savingsGoalDao();
}

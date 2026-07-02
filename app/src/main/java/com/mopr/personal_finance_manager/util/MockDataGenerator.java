package com.mopr.personal_finance_manager.util;

import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.repository.FinanceRepository;

import java.util.Calendar;

public class MockDataGenerator {

    public static void seedData(FinanceRepository repository) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);

        // Category IDs are 1-based from AppDatabase pre-population
        // 1: Food, 2: Transport, 3: Bills, 8: Salary

        // Budgets
        repository.insertBudget(new Budget(1, "EXPENSE", 3300000, start, end));
        repository.insertBudget(new Budget(3, "EXPENSE", 2186000, start, end));

        // Income
        repository.insertTransaction(new Transaction("INCOME", 6500000, 8, now, "Monthly Salary", "VND"));

        // Expenses
        repository.insertTransaction(new Transaction("EXPENSE", 1687000, 1, now, "Daily Living", "VND"));
        repository.insertTransaction(new Transaction("EXPENSE", 1200000, 3, now, "Bills", "VND"));
    }
}

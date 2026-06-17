package com.mopr.personal_finance_manager.util;

import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.data.repository.FinanceRepository;

import java.util.Calendar;

public class MockDataGenerator {

    public static void seedData(FinanceRepository repository) {
        String currentMonth = DateUtils.getCurrentBudgetMonth();
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();

        // Budgets
        repository.insertBudget(new Budget(Category.FOOD, 3300000, currentMonth));
        repository.insertBudget(new Budget(Category.HAIRCUT, 140000, currentMonth));
        repository.insertBudget(new Budget(Category.BILLS, 2186000, currentMonth));

        // Income
        repository.insertTransaction(new Transaction("INCOME", 6500000, Category.SALARY, now, "Monthly Salary", "VND"));

        // Expenses
        repository.insertTransaction(new Transaction("EXPENSE", 1687000, Category.FOOD, now, "Daily Living", "VND"));
        repository.insertTransaction(new Transaction("EXPENSE", 1200000, Category.BILLS, now, "Bills", "VND"));
        repository.insertTransaction(new Transaction("EXPENSE", 1316000, Category.OTHER, now, "Other", "VND"));
    }
}

package com.mopr.personal_finance_manager.data.logic;

import com.mopr.personal_finance_manager.data.local.CategoryDao;
import com.mopr.personal_finance_manager.data.local.RecurringRuleDao;
import com.mopr.personal_finance_manager.data.local.TransactionDao;

public class RecurringTransactionManager {
    private final RecurringRuleDao recurringRuleDao;
    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;

    public RecurringTransactionManager(RecurringRuleDao recurringRuleDao, TransactionDao transactionDao, CategoryDao categoryDao) {
        this.recurringRuleDao = recurringRuleDao;
        this.transactionDao = transactionDao;
        this.categoryDao = categoryDao;
    }

    public void checkAndGenerateTransactions() {
        // TODO: Implement logic to check recurring rules and generate transactions
    }
}

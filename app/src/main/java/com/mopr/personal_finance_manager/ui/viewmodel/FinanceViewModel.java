package com.mopr.personal_finance_manager.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mopr.personal_finance_manager.data.repository.FinanceRepository;
import com.mopr.personal_finance_manager.data.local.entity.Transaction;
import com.mopr.personal_finance_manager.data.local.entity.Budget;
import com.mopr.personal_finance_manager.data.local.entity.SavingsGoal;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {

    private final FinanceRepository repository;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repository = FinanceRepository.getInstance(application);
    }

    public LiveData<List<Transaction>> getRecentTransactions() {
        return repository.getRecentTransactions();
    }

    public LiveData<Double> getTotalBalance() {
        return repository.getTotalBalance();
    }

    public LiveData<Double> getTotalIncome(long start, long end) {
        return repository.getTotalIncome(start, end);
    }

    public LiveData<Double> getTotalExpense(long start, long end) {
        return repository.getTotalExpense(start, end);
    }

    public void insertTransaction(Transaction transaction) {
        repository.insertTransaction(transaction);
    }
}

package com.mopr.personal_finance_manager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.TransactionWithCategory;
import com.mopr.personal_finance_manager.data.model.PredictionResult;
import com.mopr.personal_finance_manager.data.repository.FinanceRepository;
import com.mopr.personal_finance_manager.util.BudgetPredictor;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.Calendar;
import java.util.List;

public class AnalyticsViewModel extends AndroidViewModel {

    private final FinanceRepository repo;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        repo = FinanceRepository.getInstance(application);
    }

    public LiveData<PredictionResult> getCurrentMonthPrediction() {
        Calendar cal = Calendar.getInstance();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);
        return repo.getBudgetPrediction(start, end);
    }

    public LiveData<Double> getNextMonthForecast() {
        return androidx.lifecycle.Transformations.map(repo.getHistoricalMonthlyExpenses(6), history -> {
            if (history == null || history.isEmpty()) return 0.0;
            return BudgetPredictor.predictNextPeriodExpense(history);
        });
    }

    public LiveData<List<CategorySum>> getCurrentMonthExpensesByCategory() {
        Calendar cal = Calendar.getInstance();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);
        return repo.getExpensesByCategory(start, end);
    }

    public LiveData<List<TransactionWithCategory>> getAnomalies() {
        Calendar cal = Calendar.getInstance();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);
        return repo.getAnomalies(start, end);
    }
}

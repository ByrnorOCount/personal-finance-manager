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
import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.MediatorLiveData;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.Calendar;
import java.util.List;

public class AnalyticsViewModel extends AndroidViewModel {

    private final FinanceRepository repo;
    private final MutableLiveData<Double> localBudgetLimit = new MutableLiveData<>(0.0);
    private final SharedPreferences prefs;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        repo = FinanceRepository.getInstance(application);
        prefs = application.getSharedPreferences("insights_prefs", Context.MODE_PRIVATE);
        localBudgetLimit.setValue((double) prefs.getFloat("local_budget_limit", 0f));
    }

    public void setLocalBudgetLimit(double limit) {
        prefs.edit().putFloat("local_budget_limit", (float) limit).apply();
        localBudgetLimit.setValue(limit);
    }

    public LiveData<Double> getLocalBudgetLimit() {
        return localBudgetLimit;
    }

    public LiveData<PredictionResult> getCurrentMonthPrediction() {
        Calendar cal = Calendar.getInstance();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);

        MediatorLiveData<PredictionResult> mediator = new MediatorLiveData<>();
        LiveData<PredictionResult> repoData = repo.getBudgetPrediction(start, end);

        mediator.addSource(repoData, prediction -> {
            if (prediction != null) {
                Double localLimit = localBudgetLimit.getValue();
                if (localLimit != null && localLimit > 0) {
                    mediator.setValue(BudgetPredictor.analyzeCurrentPeriod(prediction.currentSpend, localLimit, start, end));
                } else {
                    mediator.setValue(prediction);
                }
            }
        });

        mediator.addSource(localBudgetLimit, localLimit -> {
            PredictionResult current = repoData.getValue();
            if (current != null) {
                if (localLimit != null && localLimit > 0) {
                    mediator.setValue(BudgetPredictor.analyzeCurrentPeriod(current.currentSpend, localLimit, start, end));
                } else {
                    mediator.setValue(current);
                }
            }
        });

        return mediator;
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

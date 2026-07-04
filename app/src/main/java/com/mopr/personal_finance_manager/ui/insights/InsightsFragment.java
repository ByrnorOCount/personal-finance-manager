package com.mopr.personal_finance_manager.ui.insights;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.data.model.PredictionResult;
import com.mopr.personal_finance_manager.databinding.FragmentInsightsBinding;
import com.mopr.personal_finance_manager.ui.common.TransactionAdapter;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.viewmodel.AnalyticsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InsightsFragment extends Fragment {

    private FragmentInsightsBinding binding;
    private AnalyticsViewModel viewModel;
    private TransactionAdapter anomalyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);

        setupUI();
        observeData();
    }

    private void setupUI() {
        anomalyAdapter = new TransactionAdapter();
        binding.rvAnomalies.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAnomalies.setAdapter(anomalyAdapter);

        binding.btnSetBudget.setOnClickListener(v -> showSetLimitDialog());
    }

    private void observeData() {
        // 1. Prediction & Projections
        viewModel.getCurrentMonthPrediction().observe(getViewLifecycleOwner(), result -> {
            if (result != null) updateProjectionUI(result);
        });

        // 2. Next Month Forecast
        viewModel.getNextMonthForecast().observe(getViewLifecycleOwner(), forecast -> {
            if (forecast != null) {
                binding.tvForecastAmount.setText(CurrencyFormatter.formatVND(forecast));
            }
        });

        // 3. Anomalies
        viewModel.getAnomalies().observe(getViewLifecycleOwner(), anomalies -> {
            if (anomalies != null && !anomalies.isEmpty()) {
                binding.tvAnomaliesHeader.setVisibility(View.VISIBLE);
                binding.rvAnomalies.setVisibility(View.VISIBLE);
                anomalyAdapter.setTransactions(anomalies);
            } else {
                binding.tvAnomaliesHeader.setVisibility(View.GONE);
                binding.rvAnomalies.setVisibility(View.GONE);
            }
        });

        // 4. Bar Chart: Income vs Expense
        viewModel.getCurrentMonthIncomeByCategory().observe(getViewLifecycleOwner(), incomes -> {
            viewModel.getCurrentMonthExpensesByCategory().observe(getViewLifecycleOwner(), expenses -> {
                double totalIncome = 0;
                if (incomes != null) for (CategorySum cs : incomes) totalIncome += cs.totalAmount;

                double totalExpense = 0;
                if (expenses != null) for (CategorySum cs : expenses) totalExpense += cs.totalAmount;

                setupBarChart(totalIncome, totalExpense);
                updateSavingsRate(totalIncome, totalExpense);
            });
        });

        // 5. Pie Chart & Category Highlights
        viewModel.getCurrentMonthExpensesByCategory().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                setupPieChart(categories);
                updateTopCategory(categories);
            }
        });

        // 6. Subcategory Highlights
        viewModel.getCurrentMonthRawExpenses().observe(getViewLifecycleOwner(), allCats -> {
            if (allCats != null) {
                updateTopSubcategory(allCats);
            }
        });
    }

    private void updateProjectionUI(PredictionResult result) {
        binding.tvProjectedSpend.setText(CurrencyFormatter.formatVND(result.projectedEndSpend));
        binding.tvBurnRate.setText(CurrencyFormatter.formatVND(result.dailyBurnRate) + " / day");
        binding.tvDaysLeft.setText(result.daysRemaining + " days");

        if (result.budgetLimit <= 0) {
            binding.llLimitContainer.setVisibility(View.GONE);
            binding.tvProjectionStatus.setText("No budget set");
            binding.tvProjectionStatus.setTextColor(Color.GRAY);
        } else {
            binding.llLimitContainer.setVisibility(View.VISIBLE);
            binding.tvBudgetLimitDisplay.setText(CurrencyFormatter.formatVND(result.budgetLimit));

            int color = result.projectedEndSpend > result.budgetLimit ?
                    getContext().getColor(R.color.expense_red) : getContext().getColor(R.color.income_green);
            binding.tvProjectionStatus.setText(result.risk.toString() + " RISK");
            binding.tvProjectionStatus.setTextColor(color);
        }

        binding.tvRecommendation.setText(result.recommendation);
    }

    private void setupBarChart(double income, double expense) {
        BarChart chart = binding.barChartSummary;
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) income));
        entries.add(new BarEntry(1, (float) expense));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(getContext().getColor(R.color.income_green), getContext().getColor(R.color.expense_red));
        dataSet.setValueTextColor(getContext().getColor(R.color.text_primary));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Income", "Expense"}));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextColor(getContext().getColor(R.color.text_secondary));
        chart.getAxisLeft().setTextColor(getContext().getColor(R.color.text_secondary));
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void setupPieChart(List<CategorySum> categories) {
        PieChart chart = binding.pieChartExpenses;
        List<PieEntry> entries = new ArrayList<>();
        for (CategorySum cs : categories) {
            if (cs.totalAmount > 0) entries.add(new PieEntry((float) cs.totalAmount, cs.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        chart.setData(new PieData(dataSet));
        chart.setUsePercentValues(true);
        chart.setHoleRadius(45f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.getDescription().setEnabled(false);
        chart.setCenterText("Expenses");
        chart.setCenterTextColor(getContext().getColor(R.color.text_primary));
        chart.getLegend().setEnabled(false);
        chart.animateXY(1000, 1000);
    }

    private void updateTopCategory(List<CategorySum> categories) {
        CategorySum top = null;
        for (CategorySum cs : categories) {
            if (top == null || cs.totalAmount > top.totalAmount) top = cs;
        }
        if (top != null) binding.tvTopCategory.setText(top.category);
    }

    private void updateTopSubcategory(List<CategorySum> allCats) {
        CategorySum top = null;
        for (CategorySum cs : allCats) {
            if (top == null || cs.totalAmount > top.totalAmount) top = cs;
        }
        if (top != null) binding.tvTopSubcategory.setText(top.category);
    }

    private void updateSavingsRate(double income, double expense) {
        if (income <= 0) {
            binding.tvSavingsRate.setText("0%");
            return;
        }
        int rate = (int) Math.round(((income - expense) / income) * 100);
        binding.tvSavingsRate.setText(Math.max(0, rate) + "%");
        binding.tvSavingsRate.setTextColor(rate < 10 ? getContext().getColor(R.color.expense_red) : getContext().getColor(R.color.income_green));
    }

    private void showSetLimitDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Enter monthly limit");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set Budget Limit")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) viewModel.setLocalBudgetLimit(Double.parseDouble(val));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

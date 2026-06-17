package com.mopr.personal_finance_manager.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.model.CategoryBudgetUI;
import com.mopr.personal_finance_manager.databinding.FragmentHomeBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FinanceViewModel viewModel;
    private CategoryBudgetAdapter categoryAdapter;

    private double totalIncome = 0;
    private double totalSpent = 0;
    private double totalBudgeted = 0;
    private double initialBalance = 6000000; // Mock initial balance

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);
        setupRecyclerView();
        setupCharts();
        observeData();

        binding.tvDateRange.setText("Jun 01-30, 2026"); // Mock date range

        binding.btnAddBudget.setOnClickListener(v -> {
            // Navigate to budget planner or add budget screen
        });
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryBudgetAdapter();
        binding.rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategoryBudgets.setAdapter(categoryAdapter);
    }

    private void setupCharts() {
        setupDonutChart(binding.chartIncomeSpent);
        setupMiniChart(binding.miniChartSpent);
    }

    private void setupDonutChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleColor(Color.TRANSPARENT);
        chart.setTransparentCircleAlpha(0);
        chart.setHoleRadius(75f);
        chart.setDrawCenterText(true);
        chart.setRotationAngle(0);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.getLegend().setEnabled(false);
    }

    private void setupMiniChart(PieChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setHoleRadius(70f);
        chart.getLegend().setEnabled(false);
        chart.setDrawCenterText(true);
    }

    private void observeData() {
        Calendar cal = Calendar.getInstance();
        // Force mock month for demonstration if needed, but here we use current
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);
        String month = DateUtils.getBudgetMonth(System.currentTimeMillis());

        viewModel.getTotalIncome(start, end).observe(getViewLifecycleOwner(), income -> {
            totalIncome = income != null ? income : 0.0;
            updateDashboard();
        });

        viewModel.getTotalExpense(start, end).observe(getViewLifecycleOwner(), expense -> {
            totalSpent = expense != null ? expense : 0.0;
            updateDashboard();
        });

        viewModel.getTotalBudgetedForMonth(month).observe(getViewLifecycleOwner(), budgeted -> {
            totalBudgeted = budgeted != null ? budgeted : 0.0;
            updateDashboard();
        });

        // Observe both budgets and actual expenses to update the category list
        viewModel.getBudgetsForMonth(month).observe(getViewLifecycleOwner(), budgets -> {
            viewModel.getExpensesByCategory(start, end).observe(getViewLifecycleOwner(), expenses -> {
                combineAndSetCategoryData(budgets, expenses);
            });
        });
    }

    private void combineAndSetCategoryData(List<Budget> budgets, List<CategorySum> expenses) {
        Map<String, Double> expenseMap = new HashMap<>();
        if (expenses != null) {
            for (CategorySum sum : expenses) {
                expenseMap.put(sum.category, sum.totalAmount);
            }
        }

        List<CategoryBudgetUI> uiItems = new ArrayList<>();
        if (budgets != null) {
            for (Budget b : budgets) {
                Double spentBoxed = expenseMap.get(b.category);
                double spent = spentBoxed != null ? spentBoxed : 0.0;
                uiItems.add(new CategoryBudgetUI(b.category, b.limitAmount, spent));
            }
        }
        categoryAdapter.setItems(uiItems);
    }

    private void updateDashboard() {
        binding.tvInitialBalance.setText(CurrencyFormatter.formatVND(initialBalance));
        binding.tvTotalIncome.setText(CurrencyFormatter.formatVND(totalIncome));
        binding.tvTotalBudgeted.setText(CurrencyFormatter.formatVND(totalBudgeted));

        double provisionalBalance = initialBalance + totalIncome - totalSpent;
        binding.tvProvisionalBalance.setText(CurrencyFormatter.formatVND(provisionalBalance));

        double remaining = totalBudgeted - totalSpent;
        binding.tvRemaining.setText(CurrencyFormatter.formatVND(Math.max(0, remaining)));

        binding.tvListTotalBudgeted.setText(CurrencyFormatter.formatVND(totalBudgeted));
        binding.tvListTotalSpent.setText(CurrencyFormatter.formatVND(totalSpent));

        double saving = totalIncome - totalSpent;
        binding.tvSaving.setText(CurrencyFormatter.formatVND(Math.max(0, saving)));

        // Update Progress Bars
        binding.incomeProgress.setProgress(100);
        int budgetProgress = totalBudgeted == 0 ? 0 : (int) ((totalSpent / totalBudgeted) * 100);
        binding.budgetProgress.setProgress(Math.min(100, budgetProgress));

        // Dynamic positioning of markers (optional refinement)
        // For now, they are static in XML to match the aesthetic.

        updateChartsData();
    }

    private void updateChartsData() {
        // Main Donut Chart
        List<PieEntry> entries = new ArrayList<>();
        float spentPercent = totalIncome == 0 ? 0 : (float) (totalSpent / totalIncome);
        entries.add(new PieEntry(spentPercent, ""));
        entries.add(new PieEntry(1f - spentPercent, ""));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#FFC107"), Color.parseColor("#2C2C2C"));
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        binding.chartIncomeSpent.setData(data);
        binding.chartIncomeSpent.setCenterText("Income spent\n" + (int)(spentPercent * 100) + "%");
        binding.chartIncomeSpent.setCenterTextColor(Color.WHITE);
        binding.chartIncomeSpent.setCenterTextSize(14f);
        binding.chartIncomeSpent.invalidate();

        // Mini Chart
        List<PieEntry> miniEntries = new ArrayList<>();
        float budgetSpentPercent = totalBudgeted == 0 ? 0 : (float) (totalSpent / totalBudgeted);
        miniEntries.add(new PieEntry(budgetSpentPercent, ""));
        miniEntries.add(new PieEntry(1f - budgetSpentPercent, ""));

        PieDataSet miniDataSet = new PieDataSet(miniEntries, "");
        miniDataSet.setColors(Color.parseColor("#FFC107"), Color.parseColor("#2C2C2C"));
        miniDataSet.setDrawValues(false);

        binding.miniChartSpent.setData(new PieData(miniDataSet));
        binding.miniChartSpent.setCenterText((int)(budgetSpentPercent * 100) + "%");
        binding.miniChartSpent.setCenterTextColor(Color.WHITE);
        binding.miniChartSpent.setCenterTextSize(9f);
        binding.miniChartSpent.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

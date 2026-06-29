package com.mopr.personal_finance_manager.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.CategoryBudget;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.data.model.CategoryBudgetUI;
import com.mopr.personal_finance_manager.databinding.FragmentHomeBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final int CLR_ORANGE = Color.parseColor("#FFC107");
    private FragmentHomeBinding binding;
    private FinanceViewModel viewModel;
    private CategoryBudgetAdapter categoryAdapter;

    private MainBudget activeBudget;
    private double totalIncome = 0;
    private double totalSpent = 0;
    private double totalBudgeted = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        setupCharts();
        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void setupCharts() {
        setupDonutChart(binding.chartSaving, 84f);
        binding.chartSaving.setDrawCenterText(false);
        setupDonutChart(binding.chartIncomeSpent, 72f);
        setupDonutChart(binding.miniChartSpent, 68f);
    }

    private void setupDonutChart(com.github.mikephil.charting.charts.PieChart chart, float holeRadius) {
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleColor(Color.TRANSPARENT);
        chart.setTransparentCircleAlpha(0);
        chart.setHoleRadius(holeRadius);
        chart.setDrawCenterText(true);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setExtraOffsets(0, 0, 0, 0);
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryBudgetAdapter();
        binding.rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategoryBudgets.setAdapter(categoryAdapter);
        binding.rvCategoryBudgets.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.btnAddBudget.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.navigation_budget));
    }

    private void observeData() {
        viewModel.getActiveMainBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null) {
                activeBudget = budget;
                binding.tvDateRange.setText(budget.name);
                loadBudgetData(budget);
            } else {
                binding.tvDateRange.setText("No Active Budget");
            }
        });
    }

    private void loadBudgetData(MainBudget budget) {
        viewModel.getTotalIncome(budget.startDate, budget.endDate).observe(getViewLifecycleOwner(), income -> {
            totalIncome = income != null ? income : 0.0;
            refreshDashboard();
        });

        viewModel.getTotalExpense(budget.startDate, budget.endDate).observe(getViewLifecycleOwner(), expense -> {
            totalSpent = expense != null ? expense : 0.0;
            refreshDashboard();
        });

        viewModel.getCategoryBudgetsForMainBudget(budget.id).observe(getViewLifecycleOwner(), categoryBudgets -> {
            viewModel.getExpensesByCategory(budget.startDate, budget.endDate).observe(getViewLifecycleOwner(), expenses -> {
                rebuildCategoryList(categoryBudgets, expenses);
            });
        });
    }

    private void rebuildCategoryList(List<CategoryBudget> categoryBudgets, List<CategorySum> expenses) {
        Map<String, Double> actualMap = new HashMap<>();
        if (expenses != null) {
            for (CategorySum cs : expenses) actualMap.put(cs.category, cs.totalAmount);
        }

        List<CategoryBudgetUI> items = new ArrayList<>();
        totalBudgeted = 0;

        for (CategoryBudget cb : categoryBudgets) {
            if ("EXPENSE".equals(cb.type)) {
                Double actual = actualMap.get(cb.category);
                if (actual == null) actual = 0.0;
                items.add(new CategoryBudgetUI(cb.category, cb.limitAmount, actual));
                totalBudgeted += cb.limitAmount;
            }
        }
        categoryAdapter.setItems(items);
        refreshDashboard();
    }

    private void refreshDashboard() {
        if (binding == null || activeBudget == null) return;

        double totalFunds = activeBudget.initialBalance + totalIncome;
        double provisionalBalance = totalFunds - totalSpent;
        double saving = totalFunds - totalSpent;
        double remaining = totalBudgeted - totalSpent;

        binding.tvInitialBalance.setText(CurrencyFormatter.formatVND(activeBudget.initialBalance));
        binding.tvTotalIncome.setText(CurrencyFormatter.formatVND(totalFunds));
        binding.tvTotalBudgeted.setText(CurrencyFormatter.formatVND(totalBudgeted));
        binding.tvProvisionalBalance.setText(CurrencyFormatter.formatVND(Math.max(0, provisionalBalance)));
        binding.tvRemaining.setText(CurrencyFormatter.formatVND(Math.max(0, remaining)));
        binding.tvSaving.setText(CurrencyFormatter.formatVND(Math.max(0, saving)));
        binding.tvListTotalBudgeted.setText(CurrencyFormatter.formatVND(totalBudgeted));
        binding.tvListTotalSpent.setText(CurrencyFormatter.formatVND(totalSpent));

        float spentOfFunds = totalFunds <= 0 ? 0f : (float) Math.min(1.0, totalSpent / totalFunds);
        updateBarWeights(binding.incomeBarProvisional, binding.incomeBarSpent, 1f - spentOfFunds);

        float spentOfBudget = totalBudgeted <= 0 ? 0f : (float) Math.min(1.0, totalSpent / totalBudgeted);
        updateBarWeights(binding.budgetBarRemaining, binding.budgetBarSpent, 1f - spentOfBudget);

        if (totalBudgeted > 0 && totalSpent > totalBudgeted) {
            binding.budgetBarSpent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.expense_red)));
            binding.tvRemaining.setTextColor(requireContext().getColor(R.color.expense_red));
        } else {
            binding.budgetBarSpent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.budget_purple_accent)));
            binding.tvRemaining.setTextColor(requireContext().getColor(R.color.budget_yellow_accent));
        }

        refreshCharts();
    }

    private void updateBarWeights(View left, View right, float leftWeightFraction) {
        float leftWeight = Math.max(0.01f, leftWeightFraction);
        float rightWeight = Math.max(0.01f, 1f - leftWeightFraction);
        left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, leftWeight));
        right.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, rightWeight));
    }

    private void refreshCharts() {
        if (binding == null || activeBudget == null) return;
        double totalFunds = activeBudget.initialBalance + totalIncome;

        float spentOfIncome = totalFunds <= 0 ? 0f : (float) Math.min(1.0, totalSpent / totalFunds);
        int spentPct = Math.round(spentOfIncome * 100);
        updateDonut(binding.chartIncomeSpent, spentOfIncome, "Income\nSpent\n" + spentPct + "%", 10f, CLR_ORANGE, requireContext().getColor(R.color.donut_hole_bg));
        binding.chartIncomeSpent.setCenterTextColor(CLR_ORANGE);

        float savingOfIncome = totalFunds <= 0 ? 0f : (float) Math.max(0, (totalFunds - totalSpent) / totalFunds);
        updateDonut(binding.chartSaving, savingOfIncome, "", 0f, requireContext().getColor(R.color.saving_blue_accent), requireContext().getColor(R.color.donut_hole_bg));

        float spentOfBudget = totalBudgeted <= 0 ? 0f : (float) Math.min(1.0, totalSpent / totalBudgeted);
        int budgetPct = Math.round(spentOfBudget * 100);
        updateDonut(binding.miniChartSpent, spentOfBudget, budgetPct + "%", 9f, CLR_ORANGE, requireContext().getColor(R.color.donut_hole_bg));
    }

    private void updateDonut(com.github.mikephil.charting.charts.PieChart chart, float filledFraction, String centerLabel, float centerTextSizeSp, int primaryColor, int secondaryColor) {
        float filled = Math.max(0.001f, Math.min(1f, filledFraction));
        float empty = Math.max(0.001f, 1f - filled);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(filled, ""));
        entries.add(new PieEntry(empty, ""));
        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(primaryColor, secondaryColor);
        ds.setDrawValues(false);
        ds.setSliceSpace(0f);
        chart.setData(new PieData(ds));
        chart.setCenterText(centerLabel);
        chart.setCenterTextColor(requireContext().getColor(R.color.text_primary));
        chart.setCenterTextSize(centerTextSizeSp);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

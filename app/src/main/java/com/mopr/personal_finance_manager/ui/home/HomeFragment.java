package com.mopr.personal_finance_manager.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    // Chart ring colors (always dark regardless of theme — dashboard is a dark card)
    private static final int CLR_ORANGE = Color.parseColor("#FFC107");
    private static final int CLR_RING_BG = Color.parseColor("#2C2C2C");
    private final double initialBalance = 6_000_000; // TODO: wire to Settings/account
    private FragmentHomeBinding binding;
    private FinanceViewModel viewModel;
    private CategoryBudgetAdapter categoryAdapter;
    // ── Financial state ───────────────────────────────────────────────
    private double totalIncome = 0;
    private double totalSpent = 0;   // actual expenses recorded
    private double totalBudgeted = 0;   // sum of all budget limits this month

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

        setupCharts();
        setupRecyclerView();
        setupClickListeners();
        observeData();

        binding.tvDateRange.setText(buildMonthLabel());
    }

    // ── Setup ─────────────────────────────────────────────────────────

    private String buildMonthLabel() {
        Calendar cal = Calendar.getInstance();
        return DateUtils.getPeriodDisplayLabel("MONTH", DateUtils.getCurrentMonthKey());
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryBudgetAdapter();
        binding.rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategoryBudgets.setAdapter(categoryAdapter);
        binding.rvCategoryBudgets.setNestedScrollingEnabled(false);
    }

    private void setupCharts() {
        // Outer ring: Saving (dark gray)
        setupDonutChart(binding.chartSaving, 88f);
        binding.chartSaving.setDrawCenterText(false);

        // Inner ring: Income Spent (orange-yellow) — larger hole for the center text
        setupDonutChart(binding.chartIncomeSpent, 72f);

        // Mini donut in the budget header
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

    private void setupClickListeners() {
        binding.btnAddBudget.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.navigation_budget));
    }

    // ── Data observation ──────────────────────────────────────────────

    private void observeData() {
        Calendar cal = Calendar.getInstance();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);
        String monthKey = DateUtils.getCurrentMonthKey();

        viewModel.getTotalIncome(start, end).observe(getViewLifecycleOwner(), income -> {
            totalIncome = income != null ? income : 0.0;
            refreshDashboard();
        });

        viewModel.getTotalExpense(start, end).observe(getViewLifecycleOwner(), expense -> {
            totalSpent = expense != null ? expense : 0.0;
            refreshDashboard();
        });

        // Use legacy month query (backwards-compat) so old data still shows
        viewModel.getTotalBudgetedForMonth(monthKey).observe(getViewLifecycleOwner(), budgeted -> {
            totalBudgeted = budgeted != null ? budgeted : 0.0;
            refreshDashboard();
        });

        viewModel.getBudgetsForMonth(monthKey).observe(getViewLifecycleOwner(), budgets ->
            viewModel.getExpensesByCategory(start, end).observe(getViewLifecycleOwner(), expenses ->
                rebuildCategoryList(budgets, expenses)));
    }

    // ── Category list ─────────────────────────────────────────────────

    private void rebuildCategoryList(List<Budget> budgets, List<CategorySum> expenses) {
        Map<String, Double> expMap = new HashMap<>();
        if (expenses != null) {
            for (CategorySum cs : expenses) expMap.put(cs.category, cs.totalAmount);
        }
        List<CategoryBudgetUI> items = new ArrayList<>();
        if (budgets != null) {
            for (Budget b : budgets) {
                Double spent = expMap.get(b.category);
                items.add(new CategoryBudgetUI(b.category, b.limitAmount,
                    spent != null ? spent : 0.0));
            }
        }
        categoryAdapter.setItems(items);
    }

    // ── Dashboard refresh ─────────────────────────────────────────────

    private void refreshDashboard() {
        if (binding == null) return;

        // ── Core financial figures ────────────────────────────────────
        //
        // totalFunds     = initialBalance + totalIncome
        //   The total money available this month (what user sees as Total Income).
        //
        // provisionalBalance = totalFunds - totalSpent
        //   What you'd have left if all recorded spending is deducted.
        //   Shown in blue under the income bar.
        //
        // saving         = totalFunds - totalSpent
        //   Difference between total income (incl initial) and total spent.
        //
        // remaining      = totalBudgeted - totalSpent
        //   How much budget headroom is left. Shown in yellow under budget bar.

        double totalFunds = initialBalance + totalIncome;
        double provisionalBalance = totalFunds - totalSpent;
        double saving = totalFunds - totalSpent;
        double remaining = totalBudgeted - totalSpent;

        // ── Text fields ───────────────────────────────────────────────
        binding.tvInitialBalance.setText(CurrencyFormatter.formatVND(initialBalance));
        binding.tvTotalIncome.setText(CurrencyFormatter.formatVND(totalFunds));
        binding.tvTotalBudgeted.setText(CurrencyFormatter.formatVND(totalBudgeted));
        binding.tvProvisionalBalance.setText(CurrencyFormatter.formatVND(Math.max(0, provisionalBalance)));
        binding.tvRemaining.setText(CurrencyFormatter.formatVND(Math.max(0, remaining)));
        binding.tvSaving.setText(CurrencyFormatter.formatVND(Math.max(0, saving)));
        binding.tvListTotalBudgeted.setText(CurrencyFormatter.formatVND(totalBudgeted));
        binding.tvListTotalSpent.setText(CurrencyFormatter.formatVND(totalSpent));

        // ── Income progress bar ───────────────────────────────────────
        // Blue (Provisional) on left, Green (Spent) on right.
        float spentOfFunds = totalFunds <= 0 ? 0f
            : (float) Math.min(1.0, totalSpent / totalFunds);

        updateBarWeights(binding.incomeBarProvisional, binding.incomeBarSpent, 1f - spentOfFunds);

        // ── Budget progress bar ───────────────────────────────────────
        // Yellow (Remaining) on left, Purple (Spent) on right.
        float spentOfBudget = totalBudgeted <= 0 ? 0f
            : (float) Math.min(1.0, totalSpent / totalBudgeted);

        updateBarWeights(binding.budgetBarRemaining, binding.budgetBarSpent, 1f - spentOfBudget);

        // Over-budget: turn bar red
        if (totalBudgeted > 0 && totalSpent > totalBudgeted) {
            binding.budgetBarSpent.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.expense_red)));
            binding.tvRemaining.setTextColor(
                requireContext().getColor(R.color.expense_red));
        } else {
            binding.budgetBarSpent.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.budget_purple_accent)));
            binding.tvRemaining.setTextColor(
                requireContext().getColor(R.color.budget_yellow_accent));
        }

        // ── Charts ───────────────────────────────────────────────────
        refreshCharts();
    }

    private void updateBarWeights(View left, View right, float leftWeightFraction) {
        float leftWeight = Math.max(0.01f, leftWeightFraction);
        float rightWeight = Math.max(0.01f, 1f - leftWeightFraction);

        left.setLayoutParams(new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.MATCH_PARENT, leftWeight));
        right.setLayoutParams(new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.MATCH_PARENT, rightWeight));
    }

    private void refreshCharts() {
        if (binding == null) return;

        double totalFunds = initialBalance + totalIncome;

        // ── Main donut composition ───────────────────────────────────
        // Inner Ring: Income Spent (Orange-Yellow)
        float spentOfIncome = totalFunds <= 0 ? 0f
            : (float) Math.min(1.0, totalSpent / totalFunds);
        int spentPct = Math.round(spentOfIncome * 100);

        updateDonut(binding.chartIncomeSpent,
            spentOfIncome,
            "Spent\n" + spentPct + "%",
            11f,
            CLR_ORANGE,
            Color.TRANSPARENT);

        // Outer Ring: Saving (Dark Gray)
        // User wants saving to be the diff between total income (incl initial) and spent
        float savingOfIncome = totalFunds <= 0 ? 0f
            : (float) Math.max(0, (totalFunds - totalSpent) / totalFunds);

        updateDonut(binding.chartSaving,
            savingOfIncome,
            "",
            0f,
            CLR_RING_BG,
            Color.TRANSPARENT);

        // ── Mini donut: budget used % ─────────────────────────────────
        float spentOfBudget = totalBudgeted <= 0 ? 0f
            : (float) Math.min(1.0, totalSpent / totalBudgeted);
        int budgetPct = Math.round(spentOfBudget * 100);

        updateDonut(binding.miniChartSpent,
            spentOfBudget,
            budgetPct + "%",
            9f,
            CLR_ORANGE,
            CLR_RING_BG);
    }

    /**
     * Sets up a two-slice donut chart.
     *
     * @param filledFraction   0.0–1.0 — the primary color (filled) fraction
     * @param centerLabel      text for the hole
     * @param centerTextSizeSp sp size of the center text
     * @param primaryColor     the color for the filled fraction
     * @param secondaryColor   the color for the "empty" fraction
     */
    private void updateDonut(com.github.mikephil.charting.charts.PieChart chart,
                             float filledFraction, String centerLabel, float centerTextSizeSp,
                             int primaryColor, int secondaryColor) {
        // Always two slices — avoids MPAndroidChart single-entry crash
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

        // Use theme-aware color for center text
        int textColor = requireContext().getColor(R.color.text_primary);
        chart.setCenterTextColor(textColor);

        chart.setCenterTextSize(centerTextSizeSp);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

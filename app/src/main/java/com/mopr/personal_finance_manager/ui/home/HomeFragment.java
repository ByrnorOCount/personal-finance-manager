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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.model.CategoryBudgetUI;
import com.mopr.personal_finance_manager.databinding.FragmentHomeBinding;
import com.mopr.personal_finance_manager.ui.budget.AddBudgetDialogFragment;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {

    // Chart ring colors (always dark regardless of theme — dashboard is a dark card)
    private static final int CLR_ORANGE = Color.parseColor("#FFC107");
    private static final int CLR_RING_BG = Color.parseColor("#2C2C2C");
    private final double initialBalance = 6_000_000; // TODO: wire to Settings/account
    private FragmentHomeBinding binding;
    private FinanceViewModel viewModel;
    private CategoryBudgetAdapter categoryAdapter;
    private CategoryBudgetAdapter incomeAdapter;
    private java.util.List<Category> allCategories = new java.util.ArrayList<>();
    // ── Financial state ───────────────────────────────────────────────
    private double totalIncome = 0;
    private double totalSpent = 0;   // actual expenses recorded
    private double totalBudgeted = 0;   // sum of all budget limits this month
    private double totalIncomeGoal = 0; // sum of all income targets this month

    private boolean isIncomeExpanded = true;
    private boolean isBudgetExpanded = true;

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
        return DateUtils.getPeriodDisplayLabel("MONTH", DateUtils.getCurrentMonthKey());
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryBudgetAdapter();
        binding.rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategoryBudgets.setAdapter(categoryAdapter);
        binding.rvCategoryBudgets.setNestedScrollingEnabled(false);
        setupSwipeToDelete(binding.rvCategoryBudgets, categoryAdapter, "EXPENSE");

        incomeAdapter = new CategoryBudgetAdapter();
        binding.rvIncomeGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIncomeGoals.setAdapter(incomeAdapter);
        binding.rvIncomeGoals.setNestedScrollingEnabled(false);
        setupSwipeToDelete(binding.rvIncomeGoals, incomeAdapter, "INCOME");
    }

    private void setupSwipeToDelete(RecyclerView recyclerView, CategoryBudgetAdapter adapter, String type) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                CategoryBudgetUI item = adapter.getItems().get(position);

                Calendar cal = Calendar.getInstance();
                long start = DateUtils.getStartOfMonth(cal);
                long end = DateUtils.getEndOfMonth(cal);

                // Use one-time observer to find and delete the specific budget
                viewModel.getBudgetsInRange(type, start, end).observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<List<Budget>>() {
                    @Override
                    public void onChanged(List<Budget> budgets) {
                        viewModel.getBudgetsInRange(type, start, end).removeObserver(this);
                        if (budgets != null) {
                            for (Budget b : budgets) {
                                if (b.categoryId == item.categoryId) {
                                    viewModel.deleteBudget(b.id);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupCharts() {
        // Outer ring: Saving (dark gray)
        setupDonutChart(binding.chartSaving, 88f);
        binding.chartSaving.setDrawCenterText(false);

        // Inner ring: Income Spent (orange-yellow) — larger hole for the center text
        setupDonutChart(binding.chartIncomeSpent, 72f);

        // Mini donut in the budget header
        setupDonutChart(binding.miniChartSpent, 68f);
        // Mini donut in the income header
        setupDonutChart(binding.miniChartIncome, 68f);
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
        binding.clIncomeHeader.setOnClickListener(v -> toggleIncomeList());
        binding.ivExpandIncome.setOnClickListener(v -> toggleIncomeList());

        binding.clBudgetHeader.setOnClickListener(v -> toggleBudgetList());
        binding.ivExpandToggle.setOnClickListener(v -> toggleBudgetList());

        binding.btnAddBudget.setOnClickListener(v -> {
            String key = DateUtils.getCurrentMonthKey();
            AddBudgetDialogFragment.newInstance("MONTH", key, "EXPENSE", null)
                .show(getChildFragmentManager(), "AddBudget");
        });
        binding.btnAddIncome.setOnClickListener(v -> {
            String key = DateUtils.getCurrentMonthKey();
            AddBudgetDialogFragment.newInstance("MONTH", key, "INCOME", null)
                .show(getChildFragmentManager(), "AddIncome");
        });
    }

    // ── Data observation ──────────────────────────────────────────────

    private void observeData() {
        Calendar cal = Calendar.getInstance();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            allCategories = categories;
            refreshDashboard();
        });

        viewModel.getTotalIncome(start, end).observe(getViewLifecycleOwner(), income -> {
            totalIncome = Objects.requireNonNullElse(income, 0.0);
            refreshDashboard();
        });

        viewModel.getTotalExpense(start, end).observe(getViewLifecycleOwner(), expense -> {
            totalSpent = Objects.requireNonNullElse(expense, 0.0);
            refreshDashboard();
        });

        viewModel.getTotalBudgetedInRange("EXPENSE", start, end).observe(getViewLifecycleOwner(), budgeted -> {
            totalBudgeted = Objects.requireNonNullElse(budgeted, 0.0);
            refreshDashboard();
        });

        viewModel.getTotalBudgetedInRange("INCOME", start, end).observe(getViewLifecycleOwner(), goal -> {
            totalIncomeGoal = Objects.requireNonNullElse(goal, 0.0);
            refreshDashboard();
        });

        viewModel.getBudgetsInRange("EXPENSE", start, end).observe(getViewLifecycleOwner(), budgets ->
            viewModel.getExpensesByCategory(start, end).observe(getViewLifecycleOwner(), expenses ->
                rebuildCategoryList(budgets, expenses, categoryAdapter)));

        viewModel.getBudgetsInRange("INCOME", start, end).observe(getViewLifecycleOwner(), goals ->
            viewModel.getIncomeByCategoryInRange(start, end).observe(getViewLifecycleOwner(), earned ->
                rebuildCategoryList(goals, earned, incomeAdapter)));
    }

    // ── Category list ─────────────────────────────────────────────────

    private void rebuildCategoryList(List<Budget> budgets, List<CategorySum> progress, CategoryBudgetAdapter adapter) {
        Map<Integer, Double> progMap = new HashMap<>();
        if (progress != null) {
            for (CategorySum cs : progress) progMap.put(cs.categoryId, cs.totalAmount);
        }

        Map<Integer, Category> catMap = new HashMap<>();
        for (Category c : allCategories) catMap.put(c.id, c);

        List<CategoryBudgetUI> items = new ArrayList<>();
        if (budgets != null) {
            for (Budget b : budgets) {
                Double val = progMap.get(b.categoryId);
                Category cat = catMap.get(b.categoryId);
                if (cat != null) {
                    items.add(new CategoryBudgetUI(b.categoryId, cat.name, cat.iconRes, cat.colorRes, b.limitAmount,
                        Objects.requireNonNullElse(val, 0.0)));
                }
            }
        }
        adapter.setItems(items);
    }

    // ── Dashboard refresh ─────────────────────────────────────────────

    private void refreshDashboard() {
        if (binding == null) return;

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
        binding.tvListTotalIncomeGoal.setText(CurrencyFormatter.formatVND(totalIncomeGoal));
        binding.tvListTotalEarned.setText(CurrencyFormatter.formatVND(totalIncome));

        // ── Income progress bar ───────────────────────────────────────
        // Shows Actual Earned vs Income Goal.
        int incomeBarPct = totalIncomeGoal <= 0 ? 0
            : (int) Math.min(100, (totalIncome / totalIncomeGoal) * 100);
        binding.incomeProgress.setMax(100);
        binding.incomeProgress.setProgressCompat(incomeBarPct, true);

        // ── Budget progress bar ───────────────────────────────────────
        // Yellow fill = how much of the budget limit has been spent.
        int budgetBarPct = totalBudgeted <= 0 ? 0
            : (int) Math.min(100, (totalSpent / totalBudgeted) * 100);
        binding.budgetProgress.setMax(100);
        binding.budgetProgress.setProgressCompat(budgetBarPct, true);

        // Over-budget: turn bar red
        if (totalBudgeted > 0 && totalSpent > totalBudgeted) {
            binding.budgetProgress.setIndicatorColor(requireContext().getColor(R.color.expense_red));
            binding.tvRemaining.setTextColor(requireContext().getColor(R.color.expense_red));
        } else {
            binding.budgetProgress.setIndicatorColor(requireContext().getColor(R.color.budget_yellow_accent));
            binding.tvRemaining.setTextColor(requireContext().getColor(R.color.budget_yellow_accent));
        }

        // ── Charts ───────────────────────────────────────────────────
        refreshCharts();
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

        // ── Mini donut: income goal % ─────────────────────────────────
        float earnedOfGoal = totalIncomeGoal <= 0 ? 0f
            : (float) Math.min(1.0, totalIncome / totalIncomeGoal);
        int incomePct = Math.round(earnedOfGoal * 100);

        updateDonut(binding.miniChartIncome,
            earnedOfGoal,
            incomePct + "%",
            9f,
            CLR_ORANGE,
            CLR_RING_BG);
    }

    private void updateDonut(com.github.mikephil.charting.charts.PieChart chart,
                             float filledFraction, String centerLabel, float centerTextSizeSp,
                             int primaryColor, int secondaryColor) {
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

        int textColor = requireContext().getColor(R.color.text_primary);
        chart.setCenterTextColor(textColor);

        chart.setCenterTextSize(centerTextSizeSp);
        chart.invalidate();
    }

    private void toggleIncomeList() {
        isIncomeExpanded = !isIncomeExpanded;
        binding.rvIncomeGoals.setVisibility(isIncomeExpanded ? View.VISIBLE : View.GONE);
        binding.ivExpandIncome.animate().rotation(isIncomeExpanded ? 0 : 180).setDuration(200).start();
    }

    private void toggleBudgetList() {
        isBudgetExpanded = !isBudgetExpanded;
        binding.rvCategoryBudgets.setVisibility(isBudgetExpanded ? View.VISIBLE : View.GONE);
        binding.ivExpandToggle.animate().rotation(isBudgetExpanded ? 0 : 180).setDuration(200).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

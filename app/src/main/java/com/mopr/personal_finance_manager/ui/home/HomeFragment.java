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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Category;
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

    private FragmentHomeBinding binding;
    private FinanceViewModel viewModel;
    private CategoryBudgetAdapter categoryAdapter;

    private MainBudget activeBudget;
    private List<CategoryBudget> currentCategoryBudgets;
    private List<com.mopr.personal_finance_manager.data.local.Category> allCategories;
    private List<CategorySum> lastExpenses;
    private List<CategorySum> lastIncomes;

    private double totalIncome = 0;
    private double totalSpent = 0;
    private double totalBudgeted = 0;
    private boolean isIncomeExpanded = true;
    private boolean isExpenseExpanded = true;

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

        setupSwipeToDelete();
        setupAdapterListeners();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof CategoryBudgetAdapter.HeaderViewHolder) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                CategoryBudgetUI item = categoryAdapter.getItems().get(position);

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Remove Category Budget")
                        .setMessage("Are you sure you want to remove '" + item.categoryName + "' from this budget plan?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            // Find and delete the CategoryBudget entity
                            viewModel.getCategoryBudgetsForMainBudget(activeBudget.id).observe(getViewLifecycleOwner(), budgets -> {
                                if (budgets != null) {
                                    for (CategoryBudget cb : budgets) {
                                        if (cb.category.equals(item.categoryName)) {
                                            viewModel.deleteCategoryBudget(cb);
                                            break;
                                        }
                                    }
                                }
                            });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            categoryAdapter.notifyItemChanged(position);
                        })
                        .show();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvCategoryBudgets);
    }

    private void setupAdapterListeners() {
        categoryAdapter.setActionListener(new CategoryBudgetAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(CategoryBudgetUI item) {
                Bundle args = new Bundle();
                args.putSerializable("existingItem", item);
                Navigation.findNavController(requireView()).navigate(R.id.navigation_add_category, args);
            }

            @Override
            public void onToggleExpand(boolean isIncome) {
                if (isIncome) {
                    isIncomeExpanded = !isIncomeExpanded;
                } else {
                    isExpenseExpanded = !isExpenseExpanded;
                }
                categoryAdapter.setExpansionStates(isIncomeExpanded, isExpenseExpanded);
                rebuildCategoryList(currentCategoryBudgets, lastExpenses, lastIncomes, allCategories);
            }

            @Override
            public void onAddCategory(boolean isIncome) {
                Bundle args = new Bundle();
                args.putString("type", isIncome ? "INCOME" : "EXPENSE");
                Navigation.findNavController(requireView()).navigate(R.id.navigation_add_category, args);
            }
        });
    }

    private void setupClickListeners() {
        binding.tvDateRange.setOnClickListener(v -> {
            // Navigate to budget selection or show a picker
            Navigation.findNavController(v).navigate(R.id.navigation_budget);
        });
    }

    private void observeData() {
        viewModel.getActiveMainBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null) {
                activeBudget = budget;
                String displayName = (budget.name == null || budget.name.trim().isEmpty()) ? "My Budget Plan" : budget.name;
                binding.tvDateRange.setText(displayName);
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
            this.currentCategoryBudgets = categoryBudgets;
            viewModel.getExpensesByCategory(budget.startDate, budget.endDate).observe(getViewLifecycleOwner(), expenses -> {
                this.lastExpenses = expenses;
                viewModel.getIncomeByCategoryInRange(budget.startDate, budget.endDate).observe(getViewLifecycleOwner(), incomes -> {
                    this.lastIncomes = incomes;
                    viewModel.getAllCategories().observe(getViewLifecycleOwner(), allCats -> {
                        this.allCategories = allCats;
                        rebuildCategoryList(categoryBudgets, expenses, incomes, allCats);
                    });
                });
            });
        });
    }

    private void rebuildCategoryList(List<CategoryBudget> categoryBudgets, List<CategorySum> expenses, List<CategorySum> incomes, List<com.mopr.personal_finance_manager.data.local.Category> allCategories) {
        if (allCategories == null || categoryBudgets == null) return;

        Map<String, Double> actualMap = new HashMap<>();
        if (expenses != null) for (CategorySum cs : expenses) actualMap.put(cs.category, cs.totalAmount);
        if (incomes != null) for (CategorySum cs : incomes) actualMap.put(cs.category, cs.totalAmount);

        Map<String, com.mopr.personal_finance_manager.data.local.Category> categoryDetailsMap = new HashMap<>();
        for (com.mopr.personal_finance_manager.data.local.Category cat : allCategories) {
            categoryDetailsMap.put(cat.name, cat);
        }

        List<CategoryBudgetUI> incomeItems = new ArrayList<>();
        List<CategoryBudgetUI> expenseItems = new ArrayList<>();

        double sectionIncomeBudgeted = 0;
        double sectionIncomeActual = 0;
        double sectionExpenseBudgeted = 0;
        double sectionExpenseActual = 0;

        java.util.Set<String> handledCategories = new java.util.HashSet<>();

        // 1. Process budgeted categories
        for (CategoryBudget cb : categoryBudgets) {
            Double actual = actualMap.get(cb.category);
            if (actual == null) actual = 0.0;
            double limit = cb.limitAmount;

            com.mopr.personal_finance_manager.data.local.Category cat = categoryDetailsMap.get(cb.category);
            int catId = (cat != null) ? cat.id : 0;
            int iconRes = (cat != null && cat.iconRes != 0) ? cat.iconRes : R.drawable.ic_cat_other;
            int colorRes = (cat != null && cat.colorRes != 0) ? cat.colorRes : R.color.cat_other;

            CategoryBudgetUI uiItem = new CategoryBudgetUI(catId, cb.category, iconRes, colorRes, limit, actual, cb.type, cb.note);

            if ("INCOME".equals(cb.type)) {
                incomeItems.add(uiItem);
                sectionIncomeBudgeted += limit;
                sectionIncomeActual += actual;
            } else {
                expenseItems.add(uiItem);
                sectionExpenseBudgeted += limit;
                sectionExpenseActual += actual;
            }
            handledCategories.add(cb.category);
        }

        // 2. Add unbudgeted categories that have actual transactions
        if (expenses != null) {
            for (CategorySum cs : expenses) {
                if (!handledCategories.contains(cs.category)) {
                    com.mopr.personal_finance_manager.data.local.Category cat = categoryDetailsMap.get(cs.category);
                    int catId = (cat != null) ? cat.id : 0;
                    int iconRes = (cat != null && cat.iconRes != 0) ? cat.iconRes : R.drawable.ic_cat_other;
                    int colorRes = (cat != null && cat.colorRes != 0) ? cat.colorRes : R.color.cat_other;

                    CategoryBudgetUI uiItem = new CategoryBudgetUI(catId, cs.category, iconRes, colorRes, 0, cs.totalAmount, "EXPENSE", "");
                    expenseItems.add(uiItem);
                    sectionExpenseActual += cs.totalAmount;
                    handledCategories.add(cs.category);
                }
            }
        }

        if (incomes != null) {
            for (CategorySum cs : incomes) {
                if (!handledCategories.contains(cs.category)) {
                    com.mopr.personal_finance_manager.data.local.Category cat = categoryDetailsMap.get(cs.category);
                    int catId = (cat != null) ? cat.id : 0;
                    int iconRes = (cat != null && cat.iconRes != 0) ? cat.iconRes : R.drawable.ic_cat_other;
                    int colorRes = (cat != null && cat.colorRes != 0) ? cat.colorRes : R.color.cat_other;

                    CategoryBudgetUI uiItem = new CategoryBudgetUI(catId, cs.category, iconRes, colorRes, 0, cs.totalAmount, "INCOME", "");
                    incomeItems.add(uiItem);
                    sectionIncomeActual += cs.totalAmount;
                    handledCategories.add(cs.category);
                }
            }
        }

        List<CategoryBudgetUI> finalItems = new ArrayList<>();

        if (!expenseItems.isEmpty()) {
            finalItems.add(new CategoryBudgetUI("Expenses", sectionExpenseBudgeted, sectionExpenseActual));
            if (isExpenseExpanded) finalItems.addAll(expenseItems);
        }

        if (!incomeItems.isEmpty()) {
            finalItems.add(new CategoryBudgetUI("Incomes", sectionIncomeBudgeted, sectionIncomeActual));
            if (isIncomeExpanded) finalItems.addAll(incomeItems);
        }

        totalBudgeted = sectionExpenseBudgeted;
        categoryAdapter.setItems(finalItems);

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

        int incomeColor = com.google.android.material.color.MaterialColors.getColor(requireContext(), R.attr.colorIncome, Color.GREEN);

        float spentOfIncome = totalFunds <= 0 ? 0f : (float) Math.min(1.0, totalSpent / totalFunds);
        int spentPct = Math.round(spentOfIncome * 100);
        updateDonut(binding.chartIncomeSpent, spentOfIncome, "Income\nSpent\n" + spentPct + "%", 10f, incomeColor, requireContext().getColor(R.color.donut_hole_bg));
        binding.chartIncomeSpent.setCenterTextColor(incomeColor);

        float savingOfIncome = totalFunds <= 0 ? 0f : (float) Math.max(0, (totalFunds - totalSpent) / totalFunds);
        updateDonut(binding.chartSaving, savingOfIncome, "", 0f, requireContext().getColor(R.color.saving_blue_accent), requireContext().getColor(R.color.donut_hole_bg));
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

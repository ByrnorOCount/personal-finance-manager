package com.mopr.personal_finance_manager.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.CategoryBudget;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.data.model.CategoryBudgetUI;
import com.mopr.personal_finance_manager.databinding.FragmentInitialPlanningBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitialPlanningFragment extends Fragment {

    private FragmentInitialPlanningBinding binding;
    private FinanceViewModel viewModel;
    private MainBudget mainBudget;
    private List<CategoryBudget> incomeBudgets = new ArrayList<>();
    private List<CategoryBudget> expenseBudgets = new ArrayList<>();
    private Map<String, Double> expensePredictions = new HashMap<>();
    private PlanningBudgetAdapter incomeAdapter;
    private PlanningBudgetAdapter expenseAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInitialPlanningBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        if (getArguments() != null) {
            mainBudget = (MainBudget) getArguments().getSerializable("mainBudget");
        }

        setupRecyclerViews();
        setupClickListeners();
        setupResultListeners();
        setupDefaults();
        fetchPredictions();
        updateTotals();
    }

    private void fetchPredictions() {
        // Collect all category names
        List<String> allCats = new ArrayList<>();
        for (CategoryBudget cb : expenseBudgets) allCats.add(cb.category);

        long anchor = mainBudget != null ? mainBudget.startDate : System.currentTimeMillis();

        for (String cat : allCats) {
            viewModel.getHistoricalCategoryMonthlyExpenses(cat, 6, anchor).observe(getViewLifecycleOwner(), history -> {
                if (history != null && !history.isEmpty()) {
                    double predicted = com.mopr.personal_finance_manager.util.BudgetPredictor.predictNextPeriodExpense(history);
                    expensePredictions.put(cat, predicted);
                    expenseAdapter.setPredictions(expensePredictions);
                }
            });
        }
    }

    private void setupResultListeners() {
        getParentFragmentManager().setFragmentResultListener("add_category_result", getViewLifecycleOwner(), (requestKey, result) -> {
            CategoryBudget cb = (CategoryBudget) result.getSerializable("categoryBudget");
            if (cb != null) {
                if ("INCOME".equals(cb.type)) {
                    incomeBudgets.add(cb);
                    incomeAdapter.setItems(new ArrayList<>(incomeBudgets));
                } else {
                    expenseBudgets.add(cb);
                    expenseAdapter.setItems(new ArrayList<>(expenseBudgets));
                    fetchPredictions();
                }
                updateTotals();
            }
        });

        getParentFragmentManager().setFragmentResultListener("edit_category_result", getViewLifecycleOwner(), (requestKey, result) -> {
            CategoryBudget updated = (CategoryBudget) result.getSerializable("categoryBudget");
            String oldName = result.getString("oldName");
            if (updated != null && oldName != null) {
                if ("INCOME".equals(updated.type)) {
                    for (int i = 0; i < incomeBudgets.size(); i++) {
                        if (incomeBudgets.get(i).category.equals(oldName)) {
                            incomeBudgets.set(i, updated);
                            break;
                        }
                    }
                    incomeAdapter.setItems(new ArrayList<>(incomeBudgets));
                } else {
                    for (int i = 0; i < expenseBudgets.size(); i++) {
                        if (expenseBudgets.get(i).category.equals(oldName)) {
                            expenseBudgets.set(i, updated);
                            break;
                        }
                    }
                    expenseAdapter.setItems(new ArrayList<>(expenseBudgets));
                    fetchPredictions();
                }
                updateTotals();
            }
        });
    }

    private void setupRecyclerViews() {
        incomeAdapter = new PlanningBudgetAdapter(new PlanningBudgetAdapter.OnBudgetChangeListener() {
            @Override
            public void onBudgetChanged() { updateTotals(); }
            @Override
            public void onRemoveBudget(CategoryBudget budget) {
                incomeBudgets.remove(budget);
                incomeAdapter.setItems(new ArrayList<>(incomeBudgets));
                updateTotals();
            }
            @Override
            public void onEditBudget(CategoryBudget budget) {
                Bundle args = new Bundle();
                args.putString("type", budget.type);
                CategoryBudgetUI ui = new CategoryBudgetUI(0, budget.category, 0, 0, budget.limitAmount, 0, budget.type, budget.note);
                args.putSerializable("existingItem", ui);
                args.putBoolean("returnResult", true);
                Navigation.findNavController(requireView()).navigate(R.id.navigation_add_category, args);
            }
        });
        binding.rvIncomeBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIncomeBudgets.setAdapter(incomeAdapter);

        expenseAdapter = new PlanningBudgetAdapter(new PlanningBudgetAdapter.OnBudgetChangeListener() {
            @Override
            public void onBudgetChanged() { updateTotals(); }
            @Override
            public void onRemoveBudget(CategoryBudget budget) {
                expenseBudgets.remove(budget);
                expenseAdapter.setItems(new ArrayList<>(expenseBudgets));
                updateTotals();
            }
            @Override
            public void onEditBudget(CategoryBudget budget) {
                Bundle args = new Bundle();
                args.putString("type", budget.type);
                CategoryBudgetUI ui = new CategoryBudgetUI(0, budget.category, 0, 0, budget.limitAmount, 0, budget.type, budget.note);
                args.putSerializable("existingItem", ui);
                args.putBoolean("returnResult", true);
                Navigation.findNavController(requireView()).navigate(R.id.navigation_add_category, args);
            }
        });
        binding.rvExpenseBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenseBudgets.setAdapter(expenseAdapter);
    }

    private void setupDefaults() {
        if (incomeBudgets.isEmpty() && expenseBudgets.isEmpty()) {
            // Add some default categories
            incomeBudgets.add(new CategoryBudget(0, Category.SALARY, 20000000, "INCOME"));
            incomeBudgets.add(new CategoryBudget(0, Category.FREELANCE, 5000000, "INCOME"));
            incomeBudgets.add(new CategoryBudget(0, Category.INVESTMENT, 2000000, "INCOME"));
            incomeBudgets.add(new CategoryBudget(0, Category.GIFT, 500000, "INCOME"));

            expenseBudgets.add(new CategoryBudget(0, Category.FOOD, 4000000, "EXPENSE"));
            expenseBudgets.add(new CategoryBudget(0, Category.TRANSPORT, 1000000, "EXPENSE"));
            expenseBudgets.add(new CategoryBudget(0, Category.BILLS, 2000000, "EXPENSE"));
            expenseBudgets.add(new CategoryBudget(0, Category.SHOPPING, 1500000, "EXPENSE"));
            expenseBudgets.add(new CategoryBudget(0, Category.ENTERTAINMENT, 1000000, "EXPENSE"));
            expenseBudgets.add(new CategoryBudget(0, Category.HEALTH, 500000, "EXPENSE"));
        }

        incomeAdapter.setItems(new ArrayList<>(incomeBudgets));
        expenseAdapter.setItems(new ArrayList<>(expenseBudgets));
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnAddIncomeCategoryBtn.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("type", "INCOME");
            args.putBoolean("returnResult", true);
            Navigation.findNavController(v).navigate(R.id.navigation_add_category, args);
        });

        binding.btnAddExpenseCategoryBtn.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("type", "EXPENSE");
            args.putBoolean("returnResult", true);
            Navigation.findNavController(v).navigate(R.id.navigation_add_category, args);
        });

        View.OnClickListener saveAction = v -> {
            List<CategoryBudget> all = new ArrayList<>();
            all.addAll(incomeBudgets);
            all.addAll(expenseBudgets);
            viewModel.insertMainBudget(mainBudget, all);
            Navigation.findNavController(v).navigate(R.id.navigation_home);
        };

        binding.btnSaveTop.setOnClickListener(saveAction);
        binding.btnSaveBottom.setOnClickListener(saveAction);
        binding.btnSkip.setOnClickListener(v -> {
            viewModel.insertMainBudget(mainBudget, new ArrayList<>());
            Navigation.findNavController(v).navigate(R.id.navigation_home);
        });
    }

    private void updateTotals() {
        double totalIncome = 0;
        for (CategoryBudget b : incomeBudgets) totalIncome += b.limitAmount;
        binding.tvTotalIncomeGoal.setText(CurrencyFormatter.formatVND(totalIncome));

        double totalExpense = 0;
        for (CategoryBudget b : expenseBudgets) totalExpense += b.limitAmount;
        binding.tvTotalExpenseBudget.setText(CurrencyFormatter.formatVND(totalExpense));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

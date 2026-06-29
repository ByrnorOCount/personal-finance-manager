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
import com.mopr.personal_finance_manager.databinding.FragmentInitialPlanningBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class InitialPlanningFragment extends Fragment {

    private FragmentInitialPlanningBinding binding;
    private FinanceViewModel viewModel;
    private MainBudget mainBudget;
    private List<CategoryBudget> incomeBudgets = new ArrayList<>();
    private List<CategoryBudget> expenseBudgets = new ArrayList<>();
    private PlanningBudgetAdapter incomeAdapter;
    private PlanningBudgetAdapter expenseAdapter;

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
        setupDefaults();
        setupClickListeners();
        updateTotals();
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
        });
        binding.rvExpenseBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenseBudgets.setAdapter(expenseAdapter);
    }

    private void setupDefaults() {
        // Add some default categories
        incomeBudgets.add(new CategoryBudget(0, Category.SALARY, 20000000, "INCOME"));
        incomeBudgets.add(new CategoryBudget(0, Category.FREELANCE, 5000000, "INCOME"));

        expenseBudgets.add(new CategoryBudget(0, Category.FOOD, 4000000, "EXPENSE"));
        expenseBudgets.add(new CategoryBudget(0, Category.TRANSPORT, 1000000, "EXPENSE"));
        expenseBudgets.add(new CategoryBudget(0, Category.BILLS, 2000000, "EXPENSE"));

        incomeAdapter.setItems(new ArrayList<>(incomeBudgets));
        expenseAdapter.setItems(new ArrayList<>(expenseBudgets));
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

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

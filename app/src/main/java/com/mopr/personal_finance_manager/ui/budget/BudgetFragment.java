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
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.databinding.FragmentBudgetBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetFragment extends Fragment implements BudgetAdapter.OnBudgetClickListener {

    private FragmentBudgetBinding binding;
    private FinanceViewModel viewModel;
    private BudgetAdapter adapter;

    private String currentPeriodType = "MONTH";
    private Calendar currentCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        setupRecyclerView();
        setupPeriodToggles();
        setupNavigation();
        setupFab();

        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        updatePeriodDisplay();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter(this);
        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgets.setAdapter(adapter);
    }

    private void setupPeriodToggles() {
        binding.togglePeriodType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnDay) currentPeriodType = "DAY";
                else if (checkedId == R.id.btnWeek) currentPeriodType = "WEEK";
                else if (checkedId == R.id.btnMonth) currentPeriodType = "MONTH";

                updatePeriodDisplay();
                observeData();
            }
        });
    }

    private void setupNavigation() {
        binding.btnPrevPeriod.setOnClickListener(v -> {
            shiftPeriod(-1);
            updatePeriodDisplay();
            observeData();
        });
        binding.btnNextPeriod.setOnClickListener(v -> {
            shiftPeriod(1);
            updatePeriodDisplay();
            observeData();
        });
    }

    private void shiftPeriod(int delta) {
        switch (currentPeriodType) {
            case "DAY":
                currentCalendar.add(Calendar.DAY_OF_YEAR, delta);
                break;
            case "WEEK":
                currentCalendar.add(Calendar.WEEK_OF_YEAR, delta);
                break;
            case "MONTH":
                currentCalendar.add(Calendar.MONTH, delta);
                break;
        }
    }

    private void setupFab() {
        binding.fabAddBudget.setOnClickListener(v -> {
            String key = getCurrentPeriodKey();
            AddBudgetDialogFragment.newInstance(currentPeriodType, key, null)
                .show(getChildFragmentManager(), "AddBudget");
        });
    }

    private void updatePeriodDisplay() {
        String key = getCurrentPeriodKey();
        String label = "";
        switch (currentPeriodType) {
            case "DAY":
                label = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(currentCalendar.getTime());
                break;
            case "WEEK":
                label = "Week " + currentCalendar.get(Calendar.WEEK_OF_YEAR) + ", " + currentCalendar.get(Calendar.YEAR);
                break;
            case "MONTH":
                label = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentCalendar.getTime());
                break;
        }
        binding.tvSelectedPeriodKey.setText(label);
        binding.tvBudgetPeriod.setText(DateUtils.getPeriodDisplayLabel(currentPeriodType, key));
    }

    private String getCurrentPeriodKey() {
        switch (currentPeriodType) {
            case "DAY": return DateUtils.getDayKey(currentCalendar);
            case "WEEK": return DateUtils.getWeekKey(currentCalendar);
            default: return DateUtils.getMonthKey(currentCalendar);
        }
    }

    private void observeData() {
        String key = getCurrentPeriodKey();
        long[] range = DateUtils.getRangeForPeriod(currentPeriodType, key);

        viewModel.getBudgetsForPeriod(currentPeriodType, key).observe(getViewLifecycleOwner(), budgets -> {
            viewModel.getExpensesByCategory(range[0], range[1]).observe(getViewLifecycleOwner(), expenses -> {
                combineAndSetItems(budgets, expenses);
            });
        });
    }

    private void combineAndSetItems(List<Budget> budgets, List<CategorySum> expenses) {
        Map<String, Double> expMap = new HashMap<>();
        if (expenses != null) {
            for (CategorySum cs : expenses) expMap.put(cs.category, cs.totalAmount);
        }

        List<BudgetAdapter.BudgetUIItem> uiItems = new ArrayList<>();
        if (budgets != null) {
            for (Budget b : budgets) {
                Double spent = expMap.get(b.category);
                uiItems.add(new BudgetAdapter.BudgetUIItem(b, spent != null ? spent : 0.0));
            }
        }
        adapter.setItems(uiItems);
    }

    @Override
    public void onBudgetClick(Budget budget) {
        AddBudgetDialogFragment.newInstance(currentPeriodType, getCurrentPeriodKey(), budget)
            .show(getChildFragmentManager(), "EditBudget");
    }

    @Override
    public void onAddTransactionClick(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_add_transaction, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

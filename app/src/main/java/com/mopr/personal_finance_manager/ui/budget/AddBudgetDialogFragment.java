package com.mopr.personal_finance_manager.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.databinding.DialogAddBudgetBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.Arrays;

public class AddBudgetDialogFragment extends BottomSheetDialogFragment {

    private DialogAddBudgetBinding binding;
    private FinanceViewModel viewModel;
    private Budget existingBudget;
    private String periodType;
    private String periodKey;

    public static AddBudgetDialogFragment newInstance(String type, String key, @Nullable Budget budget) {
        AddBudgetDialogFragment fragment = new AddBudgetDialogFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("key", key);
        args.putSerializable("budget", budget);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        if (getArguments() != null) {
            periodType = getArguments().getString("type");
            periodKey = getArguments().getString("key");
            existingBudget = (Budget) getArguments().getSerializable("budget");
        }

        setupUI();
    }

    private void setupUI() {
        binding.tvPeriodInfo.setText(DateUtils.getPeriodDisplayLabel(periodType, periodKey));

        String[] categories = Category.expenseCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item,
            Arrays.stream(categories).map(c -> Category.getDisplayName(requireContext(), c)).toArray(String[]::new));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(adapter);

        if (existingBudget != null) {
            binding.tvDialogTitle.setText("Edit Budget");
            binding.etLimit.setText(String.valueOf(existingBudget.limitAmount));
            int index = Arrays.asList(categories).indexOf(existingBudget.category);
            if (index >= 0) binding.categorySpinner.setSelection(index);
            binding.btnDelete.setVisibility(View.VISIBLE);
        }

        binding.btnSave.setOnClickListener(v -> saveBudget());
        binding.btnDelete.setOnClickListener(v -> {
            viewModel.deleteBudget(existingBudget.id);
            dismiss();
        });
    }

    private void saveBudget() {
        String amountStr = binding.etLimit.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double limit = Double.parseDouble(amountStr);
        String category = Category.expenseCategories()[binding.categorySpinner.getSelectedItemPosition()];

        if (existingBudget != null) {
            existingBudget.category = category;
            existingBudget.limitAmount = limit;
            viewModel.updateBudget(existingBudget);
        } else {
            Budget b = new Budget(category, limit, periodType, periodKey);
            viewModel.upsertBudget(b);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

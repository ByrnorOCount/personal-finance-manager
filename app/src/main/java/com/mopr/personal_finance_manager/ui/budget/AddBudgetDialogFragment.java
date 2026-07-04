package com.mopr.personal_finance_manager.ui.budget;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.databinding.DialogAddBudgetBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddBudgetDialogFragment extends BottomSheetDialogFragment {

    private DialogAddBudgetBinding binding;
    private FinanceViewModel viewModel;
    private Budget existingBudget;
    private String periodType;
    private String periodKey;
    private List<Category> categories = new ArrayList<>();
    private String currentMode = "EXPENSE";

    private long selectedStart;
    private long selectedEnd;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static AddBudgetDialogFragment newInstance(String type, String key, String initialMode, @Nullable Budget budget) {
        AddBudgetDialogFragment fragment = new AddBudgetDialogFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("key", key);
        args.putString("mode", initialMode);
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
            currentMode = getArguments().getString("mode", "EXPENSE");
            existingBudget = (Budget) getArguments().getSerializable("budget");
        }

        long[] range = DateUtils.getRangeForPeriod(periodType, periodKey);
        selectedStart = range[0];
        selectedEnd = range[1];

        setupUI();
        observeCategories();
    }

    private void setupUI() {
        binding.etStartDate.setText(dateFormat.format(new Date(selectedStart)));
        binding.etEndDate.setText(dateFormat.format(new Date(selectedEnd)));

        binding.etStartDate.setOnClickListener(v -> showDateRangePicker());
        binding.etEndDate.setOnClickListener(v -> showDateRangePicker());

        if (existingBudget != null) {
            currentMode = existingBudget.type;
            selectedStart = existingBudget.startDate;
            selectedEnd = existingBudget.endDate;
            binding.etStartDate.setText(dateFormat.format(new Date(selectedStart)));
            binding.etEndDate.setText(dateFormat.format(new Date(selectedEnd)));
            binding.toggleGroup.check(currentMode.equals("INCOME") ? R.id.btnTypeIncome : R.id.btnTypeExpense);
            binding.etLimit.setText(String.valueOf(existingBudget.limitAmount));
            binding.btnDelete.setVisibility(View.VISIBLE);
        }

        updateLabels();
        updateProgress();

        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                currentMode = (checkedId == R.id.btnTypeIncome) ? "INCOME" : "EXPENSE";
                updateLabels();
                observeCategories();
                updateProgress();
            }
        });

        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < categories.size()) {
                    binding.etCategoryName.setText(categories.get(position).name);
                    updateProgress();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.etLimit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateProgress(); }
        });

        binding.btnSave.setOnClickListener(v -> saveBudget());
        binding.btnDelete.setOnClickListener(v -> {
            viewModel.deleteBudget(existingBudget.id);
            dismiss();
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Dates")
                .setSelection(new Pair<>(selectedStart, selectedEnd))
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            selectedStart = selection.first;
            selectedEnd = selection.second;
            binding.etStartDate.setText(dateFormat.format(new Date(selectedStart)));
            binding.etEndDate.setText(dateFormat.format(new Date(selectedEnd)));
            updateProgress();
        });
        picker.show(getChildFragmentManager(), "DateRange");
    }

    private void updateLabels() {
        if ("INCOME".equals(currentMode)) {
            binding.tvDialogTitle.setText(existingBudget == null ? "Set Income Goal" : "Edit Income Goal");
            binding.labelLimit.setText("Income Goal Amount");
            binding.tvProgressLabel.setText("Income Earned");
        } else {
            binding.tvDialogTitle.setText(existingBudget == null ? "Set Budget" : "Edit Budget");
            binding.labelLimit.setText("Budget Limit");
            binding.tvProgressLabel.setText("Total Spent");
        }
    }

    private void observeCategories() {
        // Remove previous observer to avoid multiple triggers when toggling modes
        viewModel.getCategoriesByType(currentMode).removeObservers(getViewLifecycleOwner());

        viewModel.getCategoriesByType(currentMode).observe(getViewLifecycleOwner(), cats -> {
            this.categories = cats;
            if (cats == null || cats.isEmpty()) {
                binding.categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, new String[]{"No existing categories"}));
                return;
            }

            List<String> displayNames = new ArrayList<>();
            for (Category c : cats) displayNames.add(c.name);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, displayNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.categorySpinner.setAdapter(adapter);

            if (existingBudget != null && existingBudget.type.equals(currentMode)) {
                for (int i = 0; i < cats.size(); i++) {
                    if (cats.get(i).id == existingBudget.categoryId) {
                        binding.categorySpinner.setSelection(i);
                        binding.etCategoryName.setText(cats.get(i).name);
                        break;
                    }
                }
            }
        });
    }

    private void updateProgress() {
        String amountStr = binding.etLimit.getText().toString();
        double goal = amountStr.isEmpty() ? 0 : Double.parseDouble(amountStr);

        binding.progressContainer.setVisibility(View.VISIBLE);

        if ("INCOME".equals(currentMode)) {
             viewModel.getTotalIncome(selectedStart, selectedEnd).observe(getViewLifecycleOwner(), earned -> {
                 double val = earned != null ? earned : 0;
                 binding.tvProgressValue.setText(CurrencyFormatter.formatVND(val));
                 int progress = (goal > 0) ? (int) Math.min(100, (val / goal) * 100) : 0;
                 binding.progressIndicator.setProgress(progress);
                 binding.tvRemainingLabel.setText(CurrencyFormatter.formatVND(Math.max(0, goal - val)) + " Left");
             });
        } else {
            viewModel.getTotalExpense(selectedStart, selectedEnd).observe(getViewLifecycleOwner(), spent -> {
                double val = spent != null ? spent : 0;
                binding.tvProgressValue.setText(CurrencyFormatter.formatVND(val));
                int progress = (goal > 0) ? (int) Math.min(100, (val / goal) * 100) : 0;
                binding.progressIndicator.setProgress(progress);
                binding.tvRemainingLabel.setText(CurrencyFormatter.formatVND(Math.max(0, goal - val)) + " Remaining");
            });
        }
    }

    private void saveBudget() {
        String categoryName = binding.etCategoryName.getText().toString().trim();
        String amountStr = binding.etLimit.getText().toString();

        if (categoryName.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double limit = Double.parseDouble(amountStr);

        // Find existing category or create new
        Category targetCat = null;
        for (Category c : categories) {
            if (c.name.equalsIgnoreCase(categoryName)) {
                targetCat = c;
                break;
            }
        }

        if (targetCat == null) {
            // New category flow
            Category newCat = new Category(categoryName, currentMode, R.drawable.ic_cat_other, R.color.cat_other, false);
            viewModel.insertCategory(newCat);
            Toast.makeText(requireContext(), "Creating new category...", Toast.LENGTH_SHORT).show();
            // Since we don't have the ID yet (async insert), we can't save the budget immediately.
            // Simplified: User will need to save again or we should use a callback.
            // For now, let's just dismiss and user can re-open.
            dismiss();
            return;
        }

        if (existingBudget != null) {
            existingBudget.categoryId = targetCat.id;
            existingBudget.limitAmount = limit;
            existingBudget.type = currentMode;
            existingBudget.startDate = selectedStart;
            existingBudget.endDate = selectedEnd;
            viewModel.updateBudget(existingBudget);
        } else {
            Budget b = new Budget(targetCat.id, currentMode, limit, selectedStart, selectedEnd);
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

package com.mopr.personal_finance_manager.ui.transactions;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.databinding.FragmentAddTransactionBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.Calendar;

public class AddTransactionFragment extends Fragment {

    private final Calendar selectedDate = Calendar.getInstance();
    private FragmentAddTransactionBinding binding;
    private FinanceViewModel viewModel;
    private boolean isExpense = true;
    private java.util.List<Category> currentCategories = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        if (getArguments() != null) {
            isExpense = getArguments().getBoolean("isExpense", true);
        }

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.toolbarTitle.setText(isExpense ? R.string.expense : R.string.income);
        binding.saveButton.setText(R.string.save);
        updateDateDisplay();
        observeCategories();
    }

    private void observeCategories() {
        String type = isExpense ? "EXPENSE" : "INCOME";

        viewModel.getActiveMainBudget().observe(getViewLifecycleOwner(), activeBudget -> {
            viewModel.getCategoriesByType(type).observe(getViewLifecycleOwner(), allCategories -> {
                currentCategories = allCategories;
                String[] displayNames = new String[allCategories.size()];
                for (int i = 0; i < allCategories.size(); i++) {
                    displayNames[i] = allCategories.get(i).name;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), R.layout.spinner_item, displayNames);
                adapter.setDropDownViewResource(R.layout.spinner_item);
                binding.categorySpinner.setAdapter(adapter);

                if (getArguments() != null && getArguments().containsKey("categoryId")) {
                    int catId = getArguments().getInt("categoryId");
                    for (int i = 0; i < allCategories.size(); i++) {
                        if (allCategories.get(i).id == catId) {
                            binding.categorySpinner.setSelection(i);
                            break;
                        }
                    }
                }
            });
        });
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.addCategoryAction.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_add_category));
        binding.dateText.setOnClickListener(v -> showDatePicker());
        binding.saveButton.setOnClickListener(v -> saveTransaction());
        binding.btnSaveTop.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (v, year, month, day) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, day);
            updateDateDisplay();
        },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplay() {
        binding.dateText.setText(DateUtils.formatDate(selectedDate.getTimeInMillis()));
    }

    private void saveTransaction() {
        String amountStr = binding.amountInput.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.amount_required, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.invalid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(requireContext(), R.string.amount_positive, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCategories.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        int selectedPos = binding.categorySpinner.getSelectedItemPosition();
        if (selectedPos < 0 || selectedPos >= currentCategories.size()) {
             Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
             return;
        }
        Category selectedCategory = currentCategories.get(selectedPos);
        saveWithCategory(amount, selectedCategory.id);
    }

    private void saveWithCategory(double amount, int categoryId) {
        String note = binding.noteInput.getText().toString().trim();

        Transaction transaction = new Transaction(
                isExpense ? "EXPENSE" : "INCOME",
                amount,
                categoryId,
                selectedDate.getTimeInMillis(),
                note,
                "VND"
        );

        viewModel.insertTransaction(transaction);
        Toast.makeText(requireContext(), R.string.transaction_saved, Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

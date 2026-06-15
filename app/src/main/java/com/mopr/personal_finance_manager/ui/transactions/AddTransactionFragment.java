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
import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.databinding.FragmentAddTransactionBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.Calendar;

public class AddTransactionFragment extends Fragment {

    private final Calendar selectedDate = Calendar.getInstance();
    private FragmentAddTransactionBinding binding;
    private FinanceViewModel viewModel;
    private boolean isExpense = true;

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

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        updateTypeToggleUI();
        updateDateDisplay();
        refreshCategorySpinner();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v ->
            Navigation.findNavController(v).navigateUp());

        binding.toggleExpense.setOnClickListener(v -> {
            if (!isExpense) {
                isExpense = true;
                updateTypeToggleUI();
                refreshCategorySpinner();
            }
        });

        binding.toggleIncome.setOnClickListener(v -> {
            if (isExpense) {
                isExpense = false;
                updateTypeToggleUI();
                refreshCategorySpinner();
            }
        });

        binding.datePickerRow.setOnClickListener(v -> showDatePicker());

        binding.saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void updateTypeToggleUI() {
        if (isExpense) {
            binding.toggleExpense.setBackgroundResource(R.drawable.bg_toggle_selected);
            binding.toggleExpense.setTextColor(requireContext().getColor(R.color.brand_primary));
            binding.toggleIncome.setBackground(null);
            binding.toggleIncome.setTextColor(requireContext().getColor(R.color.text_on_brand_secondary));
        } else {
            binding.toggleIncome.setBackgroundResource(R.drawable.bg_toggle_selected);
            binding.toggleIncome.setTextColor(requireContext().getColor(R.color.brand_primary));
            binding.toggleExpense.setBackground(null);
            binding.toggleExpense.setTextColor(requireContext().getColor(R.color.text_on_brand_secondary));
        }
    }

    private void refreshCategorySpinner() {
        String[] keys = isExpense ? Category.expenseCategories() : Category.incomeCategories();
        String[] displayNames = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            displayNames[i] = Category.getDisplayName(requireContext(), keys[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), R.layout.spinner_item, displayNames);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        binding.categorySpinner.setAdapter(adapter);
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
            binding.amountInput.setError(getString(R.string.amount_required));
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

        String[] keys = isExpense ? Category.expenseCategories() : Category.incomeCategories();
        String category = keys[binding.categorySpinner.getSelectedItemPosition()];
        String note = binding.noteInput.getText().toString().trim();

        Transaction transaction = new Transaction(
            isExpense ? "EXPENSE" : "INCOME",
            amount,
            category,
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

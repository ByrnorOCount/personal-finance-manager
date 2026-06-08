package com.mopr.personal_finance_manager.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.entity.Category;
import com.mopr.personal_finance_manager.data.local.entity.Transaction;
import com.mopr.personal_finance_manager.databinding.FragmentAddTransactionBinding;
import com.mopr.personal_finance_manager.ui.viewmodel.FinanceViewModel;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.Calendar;

public class AddTransactionFragment extends Fragment {

    private FragmentAddTransactionBinding binding;
    private FinanceViewModel viewModel;
    private Calendar selectedDate = Calendar.getInstance();
    private boolean isExpense = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        updateTypeToggle();
        updateDateDisplay();
        setupCategorySpinner();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.toggleExpense.setOnClickListener(v -> {
            isExpense = true;
            updateTypeToggle();
            setupCategorySpinner();
        });

        binding.toggleIncome.setOnClickListener(v -> {
            isExpense = false;
            updateTypeToggle();
            setupCategorySpinner();
        });

        binding.dateText.setOnClickListener(v -> showDatePicker());

        binding.saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void updateTypeToggle() {
        if (isExpense) {
            binding.toggleExpense.setBackgroundResource(R.drawable.toggle_selected_bg);
            binding.toggleExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_purple));
            binding.toggleIncome.setBackground(null);
            binding.toggleIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_semi));
        } else {
            binding.toggleIncome.setBackgroundResource(R.drawable.toggle_selected_bg);
            binding.toggleIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_purple));
            binding.toggleExpense.setBackground(null);
            binding.toggleExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_semi));
        }
    }

    private void setupCategorySpinner() {
        String[] categories = isExpense ? Category.expenseCategories() : Category.incomeCategories();
        String[] displayNames = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            displayNames[i] = Category.getDisplayName(categories[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, displayNames);
        binding.categorySpinner.setAdapter(adapter);
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateDisplay();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplay() {
        binding.dateText.setText(DateUtils.formatDate(selectedDate.getTimeInMillis()));
    }

    private void saveTransaction() {
        String amountStr = binding.amountInput.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String[] categories = isExpense ? Category.expenseCategories() : Category.incomeCategories();
        String category = categories[binding.categorySpinner.getSelectedItemPosition()];
        String note = binding.noteInput.getText().toString();

        Transaction transaction = new Transaction(
                isExpense ? "EXPENSE" : "INCOME",
                amount,
                category,
                selectedDate.getTimeInMillis(),
                note,
                "VND"
        );

        viewModel.insertTransaction(transaction);
        Toast.makeText(requireContext(), "Đã lưu giao dịch", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

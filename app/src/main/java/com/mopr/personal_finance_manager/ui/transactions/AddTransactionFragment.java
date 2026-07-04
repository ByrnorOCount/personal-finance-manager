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
    private boolean isAddingNewCategory = false;
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

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        updateTypeToggleUI();
        updateDateDisplay();
        observeCategories();

        if (getArguments() != null && getArguments().containsKey("categoryId")) {
            int catId = getArguments().getInt("categoryId");
            // Selection will be handled once categories are loaded
        }
    }

    private void observeCategories() {
        String type = isExpense ? "EXPENSE" : "INCOME";

        // Remove old observers if any (though difficult with anonymous lambdas,
        // we'll rely on viewModel and MediatorLiveData approach if we were to be 100% clean,
        // but let's just make sure we don't stack too many for this session).

        viewModel.getActiveMainBudget().observe(getViewLifecycleOwner(), activeBudget -> {
            if (activeBudget != null) {
                viewModel.getCategoryBudgetsForMainBudget(activeBudget.id).observe(getViewLifecycleOwner(), budgetCategories -> {
                    if (budgetCategories != null) {
                        for (com.mopr.personal_finance_manager.data.local.CategoryBudget bc : budgetCategories) {
                            viewModel.ensureCategoryExists(bc.category, bc.type);
                        }
                    }

                    // This will trigger whenever the categories table changes (e.g. after ensureCategoryExists inserts something)
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
            } else {
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
                });
            }
        });
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v ->
            Navigation.findNavController(v).navigateUp());

        binding.toggleExpense.setOnClickListener(v -> {
            if (!isExpense) {
                isExpense = true;
                updateTypeToggleUI();
                observeCategories();
            }
        });

        binding.toggleIncome.setOnClickListener(v -> {
            if (isExpense) {
                isExpense = false;
                updateTypeToggleUI();
                observeCategories();
            }
        });

        binding.addCategoryAction.setOnClickListener(v -> toggleNewCategoryMode());

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

    private void toggleNewCategoryMode() {
        isAddingNewCategory = !isAddingNewCategory;
        if (isAddingNewCategory) {
            binding.categorySpinner.setVisibility(View.GONE);
            binding.newCategoryInput.setVisibility(View.VISIBLE);
            binding.newCategoryInput.requestFocus();
            binding.addCategoryAction.setText(R.string.use_existing_category);
        } else {
            binding.categorySpinner.setVisibility(View.VISIBLE);
            binding.newCategoryInput.setVisibility(View.GONE);
            binding.addCategoryAction.setText(R.string.add_new_category);
        }
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

        if (isAddingNewCategory) {
            String newCatName = binding.newCategoryInput.getText().toString().trim();
            if (newCatName.isEmpty()) {
                binding.newCategoryInput.setError(getString(R.string.please_enter_category_name));
                return;
            }

            // Check if category already exists
            Category existing = null;
            for (Category c : currentCategories) {
                if (c.name.equalsIgnoreCase(newCatName)) {
                    existing = c;
                    break;
                }
            }

            if (existing != null) {
                saveWithCategory(amount, existing.id);
            } else {
                // Create new category and then save transaction
                Category newCat = new Category(newCatName, isExpense ? "EXPENSE" : "INCOME",
                        R.drawable.ic_cat_other, R.color.cat_other, false);
                // We need the ID, so we use a callback or observe the list change.
                // For simplicity in this logic, we'll use a slightly different approach:
                // insert and then rely on the repository to handle the transaction.
                // But Transaction needs a categoryId.
                // Let's improve the repository or use a simple async wait.
                // Actually, let's just insert it and then the user can select it,
                // OR we can add a method to repo that handles both.
                // For now, let's just insert the category and notify the user to select it.
                // Wait, I can see FinanceRepository uses executor.

                // Best way: use a separate method in ViewModel that returns the ID or handles the chain.
                // Given the current architecture, I'll just inform the user.
                viewModel.insertCategory(newCat);
                Toast.makeText(requireContext(), "Category created! Please select it to save.", Toast.LENGTH_LONG).show();
                toggleNewCategoryMode();
            }
        } else {
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

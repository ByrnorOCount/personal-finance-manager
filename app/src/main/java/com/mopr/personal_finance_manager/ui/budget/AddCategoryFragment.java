package com.mopr.personal_finance_manager.ui.budget;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.data.local.CategoryBudget;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.data.model.CategoryBudgetUI;
import com.mopr.personal_finance_manager.databinding.FragmentAddCategoryBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;

public class AddCategoryFragment extends Fragment {

    private FragmentAddCategoryBinding binding;
    private FinanceViewModel viewModel;
    private String currentType = "EXPENSE";
    private MainBudget activeBudget;
    private CategoryBudgetUI existingItem;
    private boolean returnResult = false;

    private int selectedIconRes = R.drawable.ic_cat_other;
    private int selectedColorRes = R.color.cat_other;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        if (getArguments() != null) {
            currentType = getArguments().getString("type", "EXPENSE");
            existingItem = (CategoryBudgetUI) getArguments().getSerializable("existingItem");
            returnResult = getArguments().getBoolean("returnResult", false);
        }

        setupUI();
        observeData();

        if (existingItem != null) {
            populateFields();
        }
    }

    private void populateFields() {
        binding.etTitle.setText(existingItem.categoryName);
        binding.etBudget.setText(String.valueOf(existingItem.budgetLimit));
        binding.etNote.setText(existingItem.note);
        currentType = existingItem.type;
        binding.toggleGroup.check(currentType.equals("INCOME") ? R.id.btnTypeIncome : R.id.btnTypeExpense);

        selectedIconRes = existingItem.iconRes;
        selectedColorRes = existingItem.colorRes;
        updateIconPreview();
        updateLabels();
    }

    private void setupUI() {
        binding.toggleGroup.check(currentType.equals("INCOME") ? R.id.btnTypeIncome : R.id.btnTypeExpense);
        updateLabels();
        updateIconPreview();

        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                currentType = (checkedId == R.id.btnTypeIncome) ? "INCOME" : "EXPENSE";
                updateLabels();
            }
        });

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnSave.setOnClickListener(v -> saveCategory());

        binding.btnSelectIcon.setOnClickListener(v -> showIconPickerDialog());
        binding.btnSelectColor.setOnClickListener(v -> showColorPickerDialog());

        binding.etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (existingItem == null) {
                    String name = s.toString().trim();
                    int icon = com.mopr.personal_finance_manager.data.model.Category.getIconRes(name);
                    int color = com.mopr.personal_finance_manager.data.model.Category.getColorRes(name);
                    if (icon != R.drawable.ic_cat_other) {
                        selectedIconRes = icon;
                        selectedColorRes = color;
                        updateIconPreview();
                    }
                }
            }
        });
    }

    private void showIconPickerDialog() {
        int[] icons = {
                R.drawable.ic_cat_salary, R.drawable.ic_cat_freelance, R.drawable.ic_cat_investment,
                R.drawable.ic_cat_gift, R.drawable.ic_cat_food, R.drawable.ic_cat_transport,
                R.drawable.ic_cat_bills, R.drawable.ic_cat_shopping, R.drawable.ic_cat_health,
                R.drawable.ic_cat_entertainment, R.drawable.ic_cat_other
        };
        String[] iconNames = {
                "Salary", "Freelance", "Investment", "Gift", "Food", "Transport",
                "Bills", "Shopping", "Health", "Entertainment", "Other"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Icon")
                .setItems(iconNames, (dialog, which) -> {
                    selectedIconRes = icons[which];
                    updateIconPreview();
                })
                .show();
    }

    private void showColorPickerDialog() {
        int[] colorResIds = {
                R.color.income_green, R.color.expense_red, R.color.cat_food,
                R.color.cat_transport, R.color.cat_bills, R.color.cat_shopping,
                R.color.cat_health, R.color.cat_entertainment, R.color.cat_other,
                R.color.brand_primary, R.color.warning_amber
        };
        String[] colorNames = {
                "Green", "Red", "Coral", "Blue", "Orange", "Purple",
                "Emerald", "Deep Orange", "Grey", "Indigo", "Amber"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Color")
                .setItems(colorNames, (dialog, which) -> {
                    selectedColorRes = colorResIds[which];
                    updateIconPreview();
                })
                .show();
    }

    private void updateIconPreview() {
        int iconRes = selectedIconRes != 0 ? selectedIconRes : R.drawable.ic_cat_other;
        int colorRes = selectedColorRes != 0 ? selectedColorRes : R.color.cat_other;
        binding.ivSelectedIconPreview.setImageResource(iconRes);
        binding.ivSelectedIconPreview.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(colorRes)));
    }

    private void updateLabels() {
        boolean isEdit = existingItem != null;
        if ("INCOME".equals(currentType)) {
            binding.budgetInputLabel.setText("Income Goal");
            binding.toolbarTitle.setText(isEdit ? "Edit Income Goal" : "Add Income Goal");
        } else {
            binding.budgetInputLabel.setText("Estimated Budget");
            binding.toolbarTitle.setText(isEdit ? "Edit Expense Budget" : "Add Expense Budget");
        }
    }

    private void observeData() {
        viewModel.getActiveMainBudget().observe(getViewLifecycleOwner(), budget -> {
            activeBudget = budget;
        });
    }

    private void saveCategory() {
        String title = binding.etTitle.getText().toString().trim();
        String budgetStr = binding.etBudget.getText().toString().trim();
        String note = binding.etNote.getText().toString().trim();

        if (title.isEmpty()) {
            binding.etTitle.setError("Title is required");
            return;
        }

        double budgetLimit = 0;
        if (!budgetStr.isEmpty()) {
            try {
                budgetLimit = Double.parseDouble(budgetStr);
            } catch (NumberFormatException e) {
                binding.etBudget.setError("Invalid amount");
                return;
            }
        }

        if (returnResult) {
            CategoryBudget cb = new CategoryBudget(0, title, budgetLimit, currentType, note);
            Bundle result = new Bundle();
            result.putSerializable("categoryBudget", cb);
            if (existingItem != null) {
                result.putString("oldName", existingItem.categoryName);
                getParentFragmentManager().setFragmentResult("edit_category_result", result);
            } else {
                getParentFragmentManager().setFragmentResult("add_category_result", result);
            }
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        if (activeBudget == null) {
            Toast.makeText(requireContext(), "No active budget plan found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingItem == null) {
            // ADD MODE
            Category newCat = new Category(title, currentType, selectedIconRes, selectedColorRes, false);
            viewModel.insertCategory(newCat);

            CategoryBudget cb = new CategoryBudget(activeBudget.id, title, budgetLimit, currentType, note);
            viewModel.insertCategoryBudget(cb);
        } else {
            // EDIT MODE (or adding budget to an unbudgeted category shown on home)
            final double finalBudgetLimit = budgetLimit;
            final String finalNote = note;
            final String finalTitle = title;
            final String finalType = currentType;

            viewModel.getCategoryBudgetsForMainBudget(activeBudget.id).observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<>() {
                @Override
                public void onChanged(java.util.List<CategoryBudget> budgets) {
                    if (budgets != null) {
                        CategoryBudget target = null;
                        for (CategoryBudget cb : budgets) {
                            if (cb.category.equals(existingItem.categoryName)) {
                                target = cb;
                                break;
                            }
                        }
                        if (target != null) {
                            target.limitAmount = finalBudgetLimit;
                            target.note = finalNote;
                            target.type = finalType;
                            target.category = finalTitle;
                            viewModel.updateCategoryBudget(target);
                        } else {
                            // It was an unbudgeted category, so we insert a new budget for it
                            CategoryBudget newCb = new CategoryBudget(activeBudget.id, finalTitle, finalBudgetLimit, finalType, finalNote);
                            viewModel.insertCategoryBudget(newCb);
                        }
                        // Important: remove observer to prevent infinite loop
                        viewModel.getCategoryBudgetsForMainBudget(activeBudget.id).removeObserver(this);
                    }
                }
            });
        }

        Toast.makeText(requireContext(), existingItem == null ? "Category added" : "Category updated", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

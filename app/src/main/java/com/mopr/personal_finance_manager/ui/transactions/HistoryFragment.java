package com.mopr.personal_finance_manager.ui.transactions;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.local.TransactionWithCategory;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.databinding.FragmentHistoryBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.ui.common.TransactionAdapter;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    // Filter modes
    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_INCOME = "INCOME";
    private static final String FILTER_EXPENSE = "EXPENSE";

    private FragmentHistoryBinding binding;
    private FinanceViewModel viewModel;
    private TransactionAdapter adapter;

    // Full unfiltered list from DB
    private List<TransactionWithCategory> allTransactions = new ArrayList<>();

    // Current filter state
    private String activeTypeFilter = FILTER_ALL;
    private Integer activeCategoryIdFilter = null; // null = no category filter

    // Current search query
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        setupRecyclerView();
        setupSearch();
        setupChips();
        setupWindowInsets();
        observeData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.historyHeader, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });
    }

    // ── RecyclerView ──────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);

        adapter.setListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionWithCategory item, int position) {
                // TODO: navigate to edit screen when implemented
                Toast.makeText(requireContext(),
                    item.category != null ? item.category.name : "Unknown", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTransactionLongClick(TransactionWithCategory item, int position) {
                showDeleteDialog(item.transaction);
            }
        });
    }

    // ── Search ────────────────────────────────────────────────────────────

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim().toLowerCase();
                binding.btnClearSearch.setVisibility(
                    searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            searchQuery = "";
        });
    }

    // ── Chip filters ──────────────────────────────────────────────────────

    private void setupChips() {
        // Type chips
        binding.chipAll.setOnClickListener(v -> {
            activeTypeFilter = FILTER_ALL;
            activeCategoryIdFilter = null;
            updateChipStates();
            populateCategoryChips(allTransactions);
            applyFilters();
        });
        binding.chipIncome.setOnClickListener(v -> {
            activeTypeFilter = FILTER_INCOME;
            activeCategoryIdFilter = null;
            updateChipStates();
            populateCategoryChips(allTransactions);
            applyFilters();
        });
        binding.chipExpense.setOnClickListener(v -> {
            activeTypeFilter = FILTER_EXPENSE;
            activeCategoryIdFilter = null;
            updateChipStates();
            populateCategoryChips(allTransactions);
            applyFilters();
        });
    }

    private void populateCategoryChips(List<TransactionWithCategory> transactions) {
        binding.cgCategoryFilters.removeAllViews();

        java.util.Map<Integer, String> chipOptions = new java.util.HashMap<>();

        // Use a background list of all categories to resolve parent names if needed
        // but for now, we'll just show chips for categories that appear in the transaction list.
        // If a transaction has a subcategory, we should ideally show the parent's chip.

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), allCats -> {
            java.util.Map<Integer, Category> allMap = new java.util.HashMap<>();
            for (Category c : allCats) allMap.put(c.id, c);

            for (TransactionWithCategory tc : transactions) {
                if (tc.category != null) {
                    // Check type filter
                    if (!activeTypeFilter.equals(FILTER_ALL)) {
                        if (activeTypeFilter.equals(FILTER_INCOME) && !"INCOME".equals(tc.transaction.type)) continue;
                        if (activeTypeFilter.equals(FILTER_EXPENSE) && !"EXPENSE".equals(tc.transaction.type)) continue;
                    }

                    Category topLevel = tc.category;
                    while (topLevel.parentId != null) {
                        Category parent = allMap.get(topLevel.parentId);
                        if (parent == null) break;
                        topLevel = parent;
                    }
                    chipOptions.put(topLevel.id, topLevel.name);
                }
            }

            renderChips(chipOptions);
        });
    }

    private void renderChips(java.util.Map<Integer, String> categories) {
        if (categories.isEmpty()) {
            binding.filterDivider.setVisibility(View.GONE);
            binding.cgCategoryFilters.setVisibility(View.GONE);
            return;
        }

        binding.filterDivider.setVisibility(View.VISIBLE);
        binding.cgCategoryFilters.setVisibility(View.VISIBLE);

        for (java.util.Map.Entry<Integer, String> entry : categories.entrySet()) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
            chip.setText(entry.getValue());
            chip.setCheckable(true);
            chip.setClickable(true);

            chip.setChipBackgroundColorResource(R.color.surface_variant);
            chip.setTextColor(requireContext().getColor(R.color.text_secondary));

            if (activeCategoryIdFilter != null && activeCategoryIdFilter.equals(entry.getKey())) {
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.brand_primary);
                chip.setTextColor(Color.WHITE);
            }

            chip.setOnClickListener(v -> {
                if (activeCategoryIdFilter != null && activeCategoryIdFilter.equals(entry.getKey())) {
                    activeCategoryIdFilter = null;
                } else {
                    activeCategoryIdFilter = entry.getKey();
                }
                updateChipStates();
                populateCategoryChips(allTransactions);
                applyFilters();
            });

            binding.cgCategoryFilters.addView(chip);
        }
    }

    private void updateChipStates() {
        // Reset top-level chips
        setChipSelected(binding.chipAll, activeTypeFilter.equals(FILTER_ALL) && activeCategoryIdFilter == null);
        setChipSelected(binding.chipIncome, activeTypeFilter.equals(FILTER_INCOME));
        setChipSelected(binding.chipExpense, activeTypeFilter.equals(FILTER_EXPENSE));
    }

    private void setChipSelected(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(requireContext().getColor(R.color.on_brand_primary));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(requireContext().getColor(R.color.text_secondary));
        }
    }

    // ── Filter logic ──────────────────────────────────────────────────────

    private void applyFilters() {
        List<TransactionWithCategory> filtered = new ArrayList<>();

        for (TransactionWithCategory item : allTransactions) {
            Transaction t = item.transaction;
            Category c = item.category;

            // 1. Type / category filter
            boolean passesType;
            if (activeCategoryIdFilter != null) {
                // If it's exactly the filtered category, OR if the category's parent is the filtered one
                passesType = (t.categoryId == activeCategoryIdFilter) || (c != null && c.parentId != null && c.parentId.equals(activeCategoryIdFilter));
            } else {
                switch (activeTypeFilter) {
                    case FILTER_INCOME:
                        passesType = t.isIncome();
                        break;
                    case FILTER_EXPENSE:
                        passesType = t.isExpense();
                        break;
                    default:
                        passesType = true;
                }
            }

            // 2. Search filter
            boolean passesSearch = true;
            if (!searchQuery.isEmpty()) {
                String note = t.note != null ? t.note.toLowerCase() : "";
                String category = c != null ? c.name.toLowerCase() : "";
                passesSearch = note.contains(searchQuery) || category.contains(searchQuery);
            }

            if (passesType && passesSearch) {
                filtered.add(item);
            }
        }

        adapter.setTransactions(filtered);
        updateTransactionCount(filtered.size());

        boolean empty = filtered.isEmpty();
        binding.rvTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void updateTransactionCount(int filtered) {
        int total = allTransactions.size();
        if (filtered == total) {
            binding.tvTransactionCount.setText(getString(R.string.transactions_count, total));
        } else {
            binding.tvTransactionCount.setText(getString(R.string.transactions_filtered_count, filtered, total));
        }
    }

    // ── Delete dialog ─────────────────────────────────────────────────────

    private void showDeleteDialog(Transaction transaction) {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_confirm_msg)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                viewModel.deleteTransaction(transaction);
                Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    // ── Data observation ──────────────────────────────────────────────────

    private void observeData() {
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions != null ? transactions : new ArrayList<>();
            populateCategoryChips(allTransactions);
            applyFilters();
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                adapter.setAllCategories(categories);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

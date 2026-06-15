package com.mopr.personal_finance_manager.ui.transactions;

import android.app.AlertDialog;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.model.Category;
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
    private List<Transaction> allTransactions = new ArrayList<>();

    // Current filter state
    private String activeTypeFilter = FILTER_ALL;
    private String activeCategoryFilter = null; // null = no category filter

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
        setupRecyclerView();
        setupSearch();
        setupChips();
        observeData();
    }

    // ── RecyclerView ──────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);

        adapter.setListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction, int position) {
                // TODO: navigate to edit screen when implemented
                Toast.makeText(requireContext(),
                    Category.getDisplayName(requireContext(), transaction.category), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTransactionLongClick(Transaction transaction, int position) {
                showDeleteDialog(transaction);
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
            activeCategoryFilter = null;
            updateChipStates();
            applyFilters();
        });
        binding.chipIncome.setOnClickListener(v -> {
            activeTypeFilter = FILTER_INCOME;
            activeCategoryFilter = null;
            updateChipStates();
            applyFilters();
        });
        binding.chipExpense.setOnClickListener(v -> {
            activeTypeFilter = FILTER_EXPENSE;
            activeCategoryFilter = null;
            updateChipStates();
            applyFilters();
        });

        // Category chips (toggle: tap again to deselect)
        binding.chipFood.setOnClickListener(v ->
            toggleCategoryFilter(Category.FOOD));
        binding.chipTransport.setOnClickListener(v ->
            toggleCategoryFilter(Category.TRANSPORT));
        binding.chipShopping.setOnClickListener(v ->
            toggleCategoryFilter(Category.SHOPPING));
        binding.chipBills.setOnClickListener(v ->
            toggleCategoryFilter(Category.BILLS));
        binding.chipHealth.setOnClickListener(v ->
            toggleCategoryFilter(Category.HEALTH));
    }

    private void toggleCategoryFilter(String category) {
        if (category.equals(activeCategoryFilter)) {
            // Deselect
            activeCategoryFilter = null;
        } else {
            activeCategoryFilter = category;
            // Category filter implies ALL types
            activeTypeFilter = FILTER_ALL;
        }
        updateChipStates();
        applyFilters();
    }

    private void updateChipStates() {
        // Reset all chips
        setChipSelected(binding.chipAll, activeTypeFilter.equals(FILTER_ALL) && activeCategoryFilter == null);
        setChipSelected(binding.chipIncome, activeTypeFilter.equals(FILTER_INCOME));
        setChipSelected(binding.chipExpense, activeTypeFilter.equals(FILTER_EXPENSE));

        setChipSelected(binding.chipFood, Category.FOOD.equals(activeCategoryFilter));
        setChipSelected(binding.chipTransport, Category.TRANSPORT.equals(activeCategoryFilter));
        setChipSelected(binding.chipShopping, Category.SHOPPING.equals(activeCategoryFilter));
        setChipSelected(binding.chipBills, Category.BILLS.equals(activeCategoryFilter));
        setChipSelected(binding.chipHealth, Category.HEALTH.equals(activeCategoryFilter));
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
        List<Transaction> filtered = new ArrayList<>();

        for (Transaction t : allTransactions) {
            // 1. Type / category filter
            boolean passesType;
            if (activeCategoryFilter != null) {
                passesType = activeCategoryFilter.equals(t.category);
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
                String category = Category.getDisplayName(requireContext(), t.category).toLowerCase();
                passesSearch = note.contains(searchQuery) || category.contains(searchQuery);
            }

            if (passesType && passesSearch) {
                filtered.add(t);
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
            applyFilters();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

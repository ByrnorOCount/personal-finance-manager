package com.mopr.personal_finance_manager.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mopr.personal_finance_manager.databinding.FragmentHomeBinding;
import com.mopr.personal_finance_manager.ui.adapter.TransactionAdapter;
import com.mopr.personal_finance_manager.ui.viewmodel.FinanceViewModel;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FinanceViewModel viewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);
        setupRecyclerView();
        observeData();

        binding.currentMonth.setText(DateUtils.formatMonthYear(System.currentTimeMillis()));
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(adapter);
        binding.rvRecentTransactions.setNestedScrollingEnabled(false);
    }

    private void observeData() {
        viewModel.getTotalBalance().observe(getViewLifecycleOwner(), balance ->
            binding.totalBalance.setText(CurrencyFormatter.formatVND(balance != null ? balance : 0.0)));

        Calendar cal = Calendar.getInstance();
        long start = DateUtils.getStartOfMonth(cal);
        long end = DateUtils.getEndOfMonth(cal);

        viewModel.getTotalIncome(start, end).observe(getViewLifecycleOwner(), income ->
            binding.totalIncome.setText(CurrencyFormatter.formatVND(income != null ? income : 0.0)));

        viewModel.getTotalExpense(start, end).observe(getViewLifecycleOwner(), expense ->
            binding.totalExpense.setText(CurrencyFormatter.formatVND(expense != null ? expense : 0.0)));

        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

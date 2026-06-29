package com.mopr.personal_finance_manager.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.databinding.FragmentBudgetBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;

public class BudgetFragment extends Fragment implements MainBudgetAdapter.OnBudgetClickListener {

    private FragmentBudgetBinding binding;
    private FinanceViewModel viewModel;
    private MainBudgetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        setupRecyclerView();
        setupFab();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        observeData();
    }

    private void setupRecyclerView() {
        adapter = new MainBudgetAdapter(this);
        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgets.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAddBudget.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_create_budget);
        });
    }

    private void observeData() {
        viewModel.getAllMainBudgets().observe(getViewLifecycleOwner(), budgets -> {
            adapter.setItems(budgets);
        });
    }

    @Override
    public void onBudgetClick(MainBudget budget) {
        viewModel.activateMainBudget(budget.id);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

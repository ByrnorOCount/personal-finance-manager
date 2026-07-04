package com.mopr.personal_finance_manager.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.databinding.FragmentSettingsBinding;
import com.mopr.personal_finance_manager.ui.common.FinanceViewModel;
import com.mopr.personal_finance_manager.util.LanguageManager;
import com.mopr.personal_finance_manager.util.ThemeManager;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private FinanceViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);

        setupUI();
        setupThemeSwitch();
        setupLanguageSelection();
        setupDataActions();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupThemeSwitch() {
        binding.darkModeSwitch.setChecked(ThemeManager.isDarkMode(requireContext()));
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(requireContext(), isChecked);
        });
    }

    private void setupLanguageSelection() {
        updateLanguageDisplay();
        binding.btnLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void setupDataActions() {
        binding.btnDeleteAll.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure you want to delete everything? This cannot be undone.")
                    .setPositiveButton("Clear Everything", (dialog, which) -> {
                        viewModel.clearAllData();
                        Toast.makeText(requireContext(), "All data cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.btnGenerateRandom.setOnClickListener(v -> {
            viewModel.generateRandomBudget();
            Toast.makeText(requireContext(), "Random budget generated!", Toast.LENGTH_LONG).show();
            Navigation.findNavController(v).navigate(R.id.navigation_home);
        });
    }

    private void updateLanguageDisplay() {
        String lang = LanguageManager.getLanguage(requireContext());
        if (lang.equals("vi")) {
            binding.tvCurrentLanguage.setText(R.string.settings_vietnamese);
        } else {
            binding.tvCurrentLanguage.setText(R.string.settings_english);
        }
    }

    private void showLanguageDialog() {
        String[] options = {getString(R.string.settings_vietnamese), getString(R.string.settings_english)};
        int checkedItem = LanguageManager.getLanguage(requireContext()).equals("vi") ? 0 : 1;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_select_language)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String selectedLang = (which == 0) ? "vi" : "en";
                    if (!selectedLang.equals(LanguageManager.getLanguage(requireContext()))) {
                        LanguageManager.setNewLocale(requireContext(), selectedLang);
                        requireActivity().recreate();
                    }
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

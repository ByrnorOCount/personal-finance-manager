package com.mopr.personal_finance_manager.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import android.util.TypedValue;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.databinding.FragmentSettingsBinding;
import com.mopr.personal_finance_manager.util.LanguageManager;
import com.mopr.personal_finance_manager.util.ThemeManager;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        setupThemeSwitch();
        setupLanguageSelection();
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);

            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (requireContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
            v.getLayoutParams().height = actionBarHeight + systemBars.top;

            return insets;
        });
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

package com.mopr.personal_finance_manager.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mopr.personal_finance_manager.databinding.FragmentSettingsBinding;
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

        binding.darkModeSwitch.setChecked(ThemeManager.isDarkMode(requireContext()));

        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(requireContext(), isChecked);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

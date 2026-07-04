package com.mopr.personal_finance_manager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mopr.personal_finance_manager.databinding.ActivityMainBinding;
import com.mopr.personal_finance_manager.databinding.BottomSheetAddOptionsBinding;
import com.mopr.personal_finance_manager.util.LanguageManager;
import com.mopr.personal_finance_manager.util.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            // FAB Click -> Show Bottom Sheet
            binding.fabAdd.setOnClickListener(v -> showAddOptions(navController));

            // Control visibility of Bottom Navigation and FAB
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.navigation_add_transaction ||
                    destination.getId() == R.id.navigation_create_budget ||
                    destination.getId() == R.id.navigation_initial_planning ||
                    destination.getId() == R.id.navigation_add_category) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                    binding.fabAdd.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    binding.fabAdd.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void showAddOptions(NavController navController) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetAddOptionsBinding sheetBinding = BottomSheetAddOptionsBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        sheetBinding.optionAddExpense.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("isExpense", true);
            navController.navigate(R.id.navigation_add_transaction, args);
            dialog.dismiss();
        });

        sheetBinding.optionAddIncome.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("isExpense", false);
            navController.navigate(R.id.navigation_add_transaction, args);
            dialog.dismiss();
        });

        sheetBinding.optionAddCategory.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_add_category);
            dialog.dismiss();
        });

        dialog.show();
    }
}

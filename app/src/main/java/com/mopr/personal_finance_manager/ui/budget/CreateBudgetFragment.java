package com.mopr.personal_finance_manager.ui.budget;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.databinding.FragmentCreateBudgetBinding;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateBudgetFragment extends Fragment {

    private FragmentCreateBudgetBinding binding;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat nameMonthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
    private SimpleDateFormat nameYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private SimpleDateFormat nameDayFormat = new SimpleDateFormat("dd", Locale.getDefault());

    private boolean userEditedName = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Default to current month
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        updateDateViews();
        updateDefaultName();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.etStartDate.setOnClickListener(v -> showDatePicker(true));
        binding.etEndDate.setOnClickListener(v -> showDatePicker(false));

        binding.etBudgetName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) userEditedName = true;
        });

        binding.btnNext.setOnClickListener(v -> {
            String name = binding.etBudgetName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a budget name", Toast.LENGTH_SHORT).show();
                return;
            }

            double initialBalance = 0;
            try {
                initialBalance = Double.parseDouble(binding.etInitialBalance.getText().toString());
            } catch (NumberFormatException ignored) {}

            MainBudget mb = new MainBudget(name,
                DateUtils.getStartOfDay(startCalendar),
                DateUtils.getEndOfDay(endCalendar),
                initialBalance, true);

            Bundle bundle = new Bundle();
            bundle.putSerializable("mainBudget", mb);
            Navigation.findNavController(v).navigate(R.id.action_createBudget_to_initialPlanning, bundle);
        });
    }

    private void updateDateViews() {
        binding.etStartDate.setText(displayFormat.format(startCalendar.getTime()));
        binding.etEndDate.setText(displayFormat.format(endCalendar.getTime()));
    }

    private void updateDefaultName() {
        if (userEditedName) return;

        String month = nameMonthFormat.format(startCalendar.getTime());
        String startDay = nameDayFormat.format(startCalendar.getTime());
        String endDay = nameDayFormat.format(endCalendar.getTime());
        String year = nameYearFormat.format(startCalendar.getTime());

        String defaultName = String.format("%s %s-%s, %s", month, startDay, endDay, year);
        binding.etBudgetName.setText(defaultName);
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = isStart ? startCalendar : endCalendar;
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateViews();
            updateDefaultName();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

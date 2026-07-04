package com.mopr.personal_finance_manager.ui.insights;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.CategorySum;
import com.mopr.personal_finance_manager.data.local.TransactionWithCategory;
import com.mopr.personal_finance_manager.data.model.PredictionResult;
import com.mopr.personal_finance_manager.ui.common.TransactionAdapter;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.viewmodel.AnalyticsViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InsightsFragment extends Fragment {

    private AnalyticsViewModel viewModel;
    private TextView tvProjectedSpend, tvProjectionStatus, tvBurnRate, tvDaysLeft, tvRecommendation, tvForecastAmount, tvAnomaliesHeader;
    private com.google.android.material.card.MaterialCardView cardRecommendation;
    private com.google.android.material.button.MaterialButton btnSetBudget;
    private PieChart pieChart;
    private RecyclerView rvAnomalies;
    private TransactionAdapter anomalyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        tvProjectedSpend = view.findViewById(R.id.tvProjectedSpend);
        tvProjectionStatus = view.findViewById(R.id.tvProjectionStatus);
        tvBurnRate = view.findViewById(R.id.tvBurnRate);
        tvDaysLeft = view.findViewById(R.id.tvDaysLeft);
        tvRecommendation = view.findViewById(R.id.tvRecommendation);
        tvForecastAmount = view.findViewById(R.id.tvForecastAmount);
        tvAnomaliesHeader = view.findViewById(R.id.tvAnomaliesHeader);
        cardRecommendation = view.findViewById(R.id.cardRecommendation);
        pieChart = view.findViewById(R.id.pieChart);
        rvAnomalies = view.findViewById(R.id.rvAnomalies);
        btnSetBudget = view.findViewById(R.id.btnSetBudget);

        btnSetBudget.setOnClickListener(v -> showSetLimitDialog());

        setupPieChart();
        setupRecyclerView();
        setupWindowInsets(view);

        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);

        observeViewModel();

        return view;
    }

    private void setupWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.insightsScrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);

        int textColor = ContextCompat.getColor(requireContext(), R.color.text_primary);
        pieChart.setEntryLabelColor(textColor);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getLegend().setEnabled(false);
    }

    private void setupRecyclerView() {
        anomalyAdapter = new TransactionAdapter();
        rvAnomalies.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAnomalies.setAdapter(anomalyAdapter);
    }

    private void showSetLimitDialog() {
        Context ctx = requireContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.set_insight_budget_title);
        builder.setMessage(R.string.set_insight_budget_desc);

        final EditText input = new EditText(ctx);
        input.setHint(R.string.enter_limit_hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        // Get current limit to pre-fill
        Double current = viewModel.getLocalBudgetLimit().getValue();
        if (current != null && current > 0) {
            input.setText(String.valueOf(current.intValue()));
        }

        android.widget.FrameLayout container = new android.widget.FrameLayout(ctx);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        params.leftMargin = margin;
        params.rightMargin = margin;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton(R.string.save_btn, (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                viewModel.setLocalBudgetLimit(Double.parseDouble(val));
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void observeViewModel() {
        viewModel.getCurrentMonthPrediction().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                updateProjectionUI(result);
            }
        });

        viewModel.getNextMonthForecast().observe(getViewLifecycleOwner(), forecast -> {
            if (forecast != null) {
                tvForecastAmount.setText(String.format(Locale.getDefault(), "%,.0f ₫", forecast));
            }
        });

        viewModel.getCurrentMonthExpensesByCategory().observe(getViewLifecycleOwner(), this::updateChartData);

        viewModel.getAnomalies().observe(getViewLifecycleOwner(), anomalies -> {
            if (anomalies != null && !anomalies.isEmpty()) {
                tvAnomaliesHeader.setVisibility(View.VISIBLE);
                rvAnomalies.setVisibility(View.VISIBLE);
                anomalyAdapter.setTransactions(anomalies);

                // Show a brief alert message if anomalies exist
                String anomalyMsg = getString(R.string.anomaly_alert_header) + ": " +
                    getString(R.string.anomaly_detected_msg, anomalies.size());
                tvAnomaliesHeader.setText(anomalyMsg);
                tvAnomaliesHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.insight_warning));
            } else {
                tvAnomaliesHeader.setVisibility(View.GONE);
                rvAnomalies.setVisibility(View.GONE);
                tvAnomaliesHeader.setText(R.string.potential_anomalies);
                tvAnomaliesHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        });
    }

    private void updateChartData(List<CategorySum> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            pieChart.clear();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (CategorySum sum : expenses) {
            entries.add(new PieEntry((float) sum.totalAmount, sum.category));
            colors.add(getCategoryColor(sum.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        pieChart.setData(data);
        pieChart.animateY(1400);
        pieChart.invalidate();
    }

    private int getCategoryColor(String categoryName) {
        if (categoryName == null) return Color.parseColor("#90A4AE");

        switch (categoryName.toLowerCase()) {
            case "food":
            case "eating out": return Color.parseColor("#FFD54F");
            case "transport":
            case "taxi":
            case "fuel": return Color.parseColor("#4FC3F7");
            case "shopping":
            case "clothes": return Color.parseColor("#F06292");
            case "health":
            case "medical": return Color.parseColor("#81C784");
            case "bills":
            case "utilities":
            case "rent": return Color.parseColor("#BA68C8");
            case "investment":
            case "savings": return Color.parseColor("#4DB6AC");
            case "entertainment":
            case "movies": return Color.parseColor("#FF8A65");
            case "salary": return Color.parseColor("#66BB6A");
            default:
                // Return a consistent color based on name hash
                int hash = categoryName.hashCode();
                String[] colors = {"#90A4AE", "#A1887F", "#7986CB", "#9575CD", "#4DD0E1", "#DCE775"};
                return Color.parseColor(colors[Math.abs(hash) % colors.length]);
        }
    }

    private void updateProjectionUI(PredictionResult result) {
        tvProjectedSpend.setText(CurrencyFormatter.formatVND(result.projectedEndSpend));
        tvBurnRate.setText(getString(R.string.daily_burn_rate_value, CurrencyFormatter.formatVND(result.dailyBurnRate)));
        tvDaysLeft.setText(getString(R.string.days_remaining_label, result.daysRemaining));

        int statusColor;
        String statusText;

        if (result.budgetLimit <= 0) {
            btnSetBudget.setVisibility(View.VISIBLE);
            statusColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
            statusText = getString(R.string.no_budget_set);
            tvProjectionStatus.setText(statusText);
            tvProjectionStatus.setTextColor(statusColor);
            cardRecommendation.setStrokeColor(android.content.res.ColorStateList.valueOf(statusColor));
            tvRecommendation.setText(result.recommendation);
            return;
        }

        btnSetBudget.setVisibility(View.GONE);

        switch (result.risk) {
            case HIGH:
                statusColor = ContextCompat.getColor(requireContext(), R.color.insight_critical);
                statusText = getString(R.string.risk_high);
                break;
            case MEDIUM:
                statusColor = ContextCompat.getColor(requireContext(), R.color.insight_warning);
                statusText = getString(R.string.risk_medium);
                break;
            case LOW:
            default:
                statusColor = ContextCompat.getColor(requireContext(), R.color.insight_positive);
                statusText = getString(R.string.risk_low);
                break;
        }

        // Custom messages for over-budget or close to budget
        StringBuilder sb = new StringBuilder();
        if (result.budgetLimit > 0 && result.projectedEndSpend > result.budgetLimit) {
            double overAmount = result.projectedEndSpend - result.budgetLimit;
            sb.append(getString(R.string.overbudget_alert_header)).append(": ")
              .append(getString(R.string.over_budget_projection_msg, CurrencyFormatter.formatVND(overAmount)))
              .append(" ");
        }
        sb.append(result.recommendation);

        tvRecommendation.setText(sb.toString());
        tvProjectionStatus.setText(statusText);
        tvProjectionStatus.setTextColor(statusColor);
        cardRecommendation.setStrokeColor(android.content.res.ColorStateList.valueOf(statusColor));
    }
}

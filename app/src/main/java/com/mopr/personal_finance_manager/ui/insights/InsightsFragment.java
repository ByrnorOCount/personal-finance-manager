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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.mopr.personal_finance_manager.viewmodel.AnalyticsViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InsightsFragment extends Fragment {

    private AnalyticsViewModel viewModel;
    private TextView tvProjectedSpend, tvProjectionStatus, tvBurnRate, tvDaysLeft, tvRecommendation, tvForecastAmount, tvAnomaliesHeader;
    private View cardRecommendation;
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

        setupPieChart();
        setupRecyclerView();

        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);

        observeViewModel();

        return view;
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getLegend().setEnabled(false);
    }

    private void setupRecyclerView() {
        anomalyAdapter = new TransactionAdapter();
        rvAnomalies.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAnomalies.setAdapter(anomalyAdapter);
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
                anomalyAdapter.setTransactions(anomalies);
            } else {
                tvAnomaliesHeader.setVisibility(View.GONE);
                anomalyAdapter.setTransactions(new ArrayList<>());
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
        data.setValueTextColor(Color.YELLOW);

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
        tvProjectedSpend.setText(String.format(Locale.getDefault(), "%,.0f ₫", result.projectedEndSpend));
        tvBurnRate.setText(String.format(Locale.getDefault(), "%,.0f ₫ / day", result.dailyBurnRate));
        tvDaysLeft.setText(String.format(Locale.getDefault(), "%d days", result.daysRemaining));
        tvRecommendation.setText(result.recommendation);

        int statusColor;
        String statusText;

        switch (result.risk) {
            case HIGH:
                statusColor = Color.parseColor("#EF5350"); // Red
                statusText = getString(R.string.risk_high);
                break;
            case MEDIUM:
                statusColor = Color.parseColor("#FFA726"); // Orange
                statusText = getString(R.string.risk_medium);
                break;
            case LOW:
            default:
                statusColor = Color.parseColor("#4CAF50"); // Green
                statusText = getString(R.string.risk_low);
                break;
        }

        tvProjectionStatus.setText(statusText);
        tvProjectionStatus.setTextColor(statusColor);

        // Update recommendation card stroke color
        if (cardRecommendation instanceof com.google.android.material.card.MaterialCardView) {
            ((com.google.android.material.card.MaterialCardView) cardRecommendation).setStrokeColor(ColorStateList.valueOf(statusColor));
        }
    }
}

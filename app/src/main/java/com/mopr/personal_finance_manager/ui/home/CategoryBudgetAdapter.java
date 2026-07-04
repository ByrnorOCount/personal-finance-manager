package com.mopr.personal_finance_manager.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.color.MaterialColors;
import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.model.CategoryBudgetUI;
import com.mopr.personal_finance_manager.databinding.ItemCategoryBudgetBinding;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(CategoryBudgetUI item);
        void onToggleExpand(boolean isIncome);
        void onAddCategory(boolean isIncome);
    }

    private List<CategoryBudgetUI> items = new ArrayList<>();
    private OnCategoryActionListener actionListener;
    private boolean isIncomeExpanded = true;
    private boolean isExpenseExpanded = true;

    public void setActionListener(OnCategoryActionListener listener) {
        this.actionListener = listener;
    }

    public List<CategoryBudgetUI> getItems() {
        return items;
    }

    public void setItems(List<CategoryBudgetUI> newItems) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                CategoryBudgetUI oi = items.get(o), ni = newItems.get(n);
                if (oi.viewType != ni.viewType) return false;
                if (oi.viewType == CategoryBudgetUI.TYPE_HEADER) return oi.categoryName.equals(ni.categoryName);
                return oi.categoryId == ni.categoryId;
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                CategoryBudgetUI oi = items.get(o), ni = newItems.get(n);
                return oi.budgetLimit == ni.budgetLimit &&
                       oi.spentAmount == ni.spentAmount &&
                       oi.totalBudgetedForSection == ni.totalBudgetedForSection &&
                       oi.totalSpentForSection == ni.totalSpentForSection;
            }
        });
        items = new ArrayList<>(newItems);
        result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == CategoryBudgetUI.TYPE_HEADER) {
            return new HeaderViewHolder(parent, inflater);
        }
        return new ViewHolder(ItemCategoryBudgetBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bind(items.get(position), actionListener);
        } else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(items.get(position), actionListener, isIncomeExpanded, isExpenseExpanded);
        }
    }

    public void toggleIncomeExpansion() {
        this.isIncomeExpanded = !isIncomeExpanded;
    }

    public void toggleExpenseExpansion() {
        this.isExpenseExpanded = !isExpenseExpanded;
    }

    public void setExpansionStates(boolean incomeExpanded, boolean expenseExpanded) {
        this.isIncomeExpanded = incomeExpanded;
        this.isExpenseExpanded = expenseExpanded;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final android.view.View incomeRoot;
        private final android.view.View expenseRoot;

        HeaderViewHolder(ViewGroup parent, LayoutInflater inflater) {
            super(new android.widget.FrameLayout(parent.getContext()));
            android.widget.FrameLayout container = (android.widget.FrameLayout) itemView;
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            incomeRoot = inflater.inflate(com.mopr.personal_finance_manager.R.layout.item_home_income_header, container, false);
            expenseRoot = inflater.inflate(com.mopr.personal_finance_manager.R.layout.item_home_expense_header, container, false);

            container.addView(incomeRoot);
            container.addView(expenseRoot);
        }

        void bind(CategoryBudgetUI item, OnCategoryActionListener listener, boolean isIncomeExpanded, boolean isExpenseExpanded) {
            boolean isIncome = "Incomes".equals(item.categoryName);
            incomeRoot.setVisibility(isIncome ? android.view.View.VISIBLE : android.view.View.GONE);
            expenseRoot.setVisibility(isIncome ? android.view.View.GONE : android.view.View.VISIBLE);

            if (isIncome) {
                android.widget.TextView tvGoalValue = incomeRoot.findViewById(com.mopr.personal_finance_manager.R.id.tvIncomeGoalValue);
                android.widget.TextView tvEarnedValue = incomeRoot.findViewById(com.mopr.personal_finance_manager.R.id.tvIncomeEarnedValue);
                com.github.mikephil.charting.charts.PieChart chart = incomeRoot.findViewById(com.mopr.personal_finance_manager.R.id.incomeProgressChart);

                tvGoalValue.setText(CurrencyFormatter.formatVND(item.totalBudgetedForSection));
                tvEarnedValue.setText(CurrencyFormatter.formatVND(item.totalSpentForSection));

                float progress = item.totalBudgetedForSection <= 0 ? 0f : (float)(item.totalSpentForSection / item.totalBudgetedForSection);
                updateMiniChart(chart, progress, MaterialColors.getColor(chart, R.attr.colorIncome));

                android.widget.ImageView ivToggle = incomeRoot.findViewById(com.mopr.personal_finance_manager.R.id.ivExpandToggle);
                ivToggle.setImageResource(isIncomeExpanded ? com.mopr.personal_finance_manager.R.drawable.ic_chevron_up : com.mopr.personal_finance_manager.R.drawable.ic_arrow_downward);
                ivToggle.setOnClickListener(v -> { if (listener != null) listener.onToggleExpand(true); });

                android.widget.ImageView ivAdd = incomeRoot.findViewById(com.mopr.personal_finance_manager.R.id.btnAddIncome);
                ivAdd.setOnClickListener(v -> { if (listener != null) listener.onAddCategory(true); });
            } else {
                android.widget.TextView tvBudgetValue = expenseRoot.findViewById(com.mopr.personal_finance_manager.R.id.tvTotalBudgetedValue);
                android.widget.TextView tvSpentValue = expenseRoot.findViewById(com.mopr.personal_finance_manager.R.id.tvTotalSpentValue);
                com.github.mikephil.charting.charts.PieChart chart = expenseRoot.findViewById(com.mopr.personal_finance_manager.R.id.expenseProgressChart);

                tvBudgetValue.setText(CurrencyFormatter.formatVND(item.totalBudgetedForSection));
                tvSpentValue.setText(CurrencyFormatter.formatVND(item.totalSpentForSection));

                float progress = item.totalBudgetedForSection <= 0 ? 0f : (float)(item.totalSpentForSection / item.totalBudgetedForSection);
                updateMiniChart(chart, progress, MaterialColors.getColor(chart, R.attr.colorExpense));

                android.widget.ImageView ivToggle = expenseRoot.findViewById(com.mopr.personal_finance_manager.R.id.ivExpandToggle);
                ivToggle.setImageResource(isExpenseExpanded ? com.mopr.personal_finance_manager.R.drawable.ic_chevron_up : com.mopr.personal_finance_manager.R.drawable.ic_arrow_downward);
                ivToggle.setOnClickListener(v -> { if (listener != null) listener.onToggleExpand(false); });

                android.widget.ImageView ivAdd = expenseRoot.findViewById(com.mopr.personal_finance_manager.R.id.btnAddExpense);
                ivAdd.setOnClickListener(v -> { if (listener != null) listener.onAddCategory(false); });
            }
        }

        private void updateMiniChart(com.github.mikephil.charting.charts.PieChart chart, float progress, int color) {
            chart.getDescription().setEnabled(false);
            chart.setUsePercentValues(false);
            chart.setDrawHoleEnabled(true);
            chart.setHoleColor(Color.TRANSPARENT);
            chart.setTransparentCircleAlpha(0);
            chart.setHoleRadius(68f);
            chart.setDrawCenterText(true);
            chart.setRotationEnabled(false);
            chart.setHighlightPerTapEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.setTouchEnabled(false);

            int pct = Math.round(progress * 100);
            chart.setCenterText(pct + "%");
            chart.setCenterTextSize(9f);
            chart.setCenterTextColor(MaterialColors.getColor(chart, com.google.android.material.R.attr.colorOnSurface));

            float filled = Math.max(0.001f, Math.min(1f, progress));
            float empty = Math.max(0.001f, 1f - filled);
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(filled, ""));
            entries.add(new PieEntry(empty, ""));
            PieDataSet ds = new PieDataSet(entries, "");
            ds.setColors(color, chart.getContext().getColor(R.color.donut_hole_bg));
            ds.setDrawValues(false);
            ds.setSliceSpace(0f);
            chart.setData(new PieData(ds));
            chart.invalidate();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBudgetBinding b;

        ViewHolder(ItemCategoryBudgetBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(CategoryBudgetUI item, OnCategoryActionListener listener) {
            Context ctx = itemView.getContext();

            b.btnAddTransaction.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item);
            });

            b.tvCategoryName.setText(item.categoryName);
            int iconRes = item.iconRes != 0 ? item.iconRes : com.mopr.personal_finance_manager.R.drawable.ic_cat_other;
            int colorRes = item.colorRes != 0 ? item.colorRes : com.mopr.personal_finance_manager.R.color.cat_other;
            b.ivCategoryIcon.setImageResource(iconRes);
            b.ivCategoryIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ctx.getColor(colorRes)));

            b.tvSpentAmount.setText(CurrencyFormatter.formatVND(item.spentAmount));
            b.tvBudgetAmount.setText(CurrencyFormatter.formatVND(item.budgetLimit));

            double progress = item.budgetLimit == 0 ? 0.0 : (item.spentAmount / item.budgetLimit);
            int pct = (int) Math.round(progress * 100);

            android.widget.LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) b.categoryProgressIndicator.getLayoutParams();
            lp.weight = (float) Math.min(100, pct);
            b.categoryProgressIndicator.setLayoutParams(lp);

            b.tvProgressPercent.setText(String.format(Locale.getDefault(), " · %.2f%%", progress * 100));

            boolean isIncome = item.type != null && item.type.equals("INCOME");
            b.tvBudgetLabel.setText(isIncome ? ctx.getString(R.string.budgeted_label) : ctx.getString(R.string.budgeted_label));

            // Handle note visibility
            if (item.note != null && !item.note.isEmpty()) {
                b.tvNote.setVisibility(android.view.View.VISIBLE);
                b.tvNote.setText(item.note);
            } else {
                b.tvNote.setVisibility(android.view.View.GONE);
            }

            if (isIncome) {
                b.tvSpentAmount.setTextColor(MaterialColors.getColor(b.tvSpentAmount, R.attr.colorIncome));
                b.tvSpentLabel.setText(ctx.getString(R.string.total_income_label));

                if (item.spentAmount >= item.budgetLimit && item.budgetLimit > 0) {
                    b.tvRemainingAmount.setText(ctx.getString(R.string.goal_reached_label));
                    b.tvRemainingAmount.setTextColor(MaterialColors.getColor(b.tvRemainingAmount, R.attr.colorIncome));
                } else {
                    double diff = item.budgetLimit - item.spentAmount;
                    b.tvRemainingAmount.setText(CurrencyFormatter.formatVND(Math.max(0, diff)) + " " + ctx.getString(R.string.left_label));
                    b.tvRemainingAmount.setTextColor(MaterialColors.getColor(b.tvRemainingAmount, R.attr.colorIncome));
                }
                b.categoryProgressIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MaterialColors.getColor(b.categoryProgressIndicator, R.attr.colorIncome)));
            } else {
                b.tvSpentAmount.setTextColor(MaterialColors.getColor(b.tvSpentAmount, com.google.android.material.R.attr.colorOnSurface));
                b.tvSpentLabel.setText(ctx.getString(R.string.spent_label));

                boolean isOver = item.spentAmount > item.budgetLimit;
                if (isOver) {
                    double over = item.spentAmount - item.budgetLimit;
                    b.tvRemainingAmount.setText(CurrencyFormatter.formatVND(over) + " " + ctx.getString(R.string.over_label));
                    b.tvRemainingAmount.setTextColor(MaterialColors.getColor(b.tvRemainingAmount, R.attr.colorExpense));
                } else {
                    double remaining = item.getRemaining();
                    b.tvRemainingAmount.setText(CurrencyFormatter.formatVND(remaining) + " " + ctx.getString(R.string.left_label));
                    b.tvRemainingAmount.setTextColor(MaterialColors.getColor(b.tvRemainingAmount, R.attr.colorIncome));
                }
                b.categoryProgressIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MaterialColors.getColor(b.categoryProgressIndicator, R.attr.colorExpense)));
            }
        }
    }
}

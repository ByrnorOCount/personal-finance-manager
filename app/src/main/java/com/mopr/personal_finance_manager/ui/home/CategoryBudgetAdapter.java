package com.mopr.personal_finance_manager.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.data.model.CategoryBudgetUI;
import com.mopr.personal_finance_manager.databinding.ItemCategoryBudgetBinding;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder> {

    private List<CategoryBudgetUI> items = new ArrayList<>();

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
                return items.get(o).category.equals(newItems.get(n).category);
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                CategoryBudgetUI oi = items.get(o), ni = newItems.get(n);
                return oi.budgetLimit == ni.budgetLimit && oi.spentAmount == ni.spentAmount;
            }
        });
        items = new ArrayList<>(newItems);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCategoryBudgetBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBudgetBinding b;

        ViewHolder(ItemCategoryBudgetBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(CategoryBudgetUI item) {
            Context ctx = itemView.getContext();

            b.tvCategoryName.setText(Category.getDisplayName(ctx, item.category));
            b.ivCategoryIcon.setImageResource(Category.getIconRes(item.category));

            // Category color circle background
            int catColor = ctx.getColor(Category.getColorRes(item.category));
            b.ivCategoryIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(catColor));

            b.tvSpentAmount.setText(CurrencyFormatter.formatVND(item.spentAmount));
            b.tvBudgetAmount.setText(CurrencyFormatter.formatVND(item.budgetLimit));

            int pct = item.getProgress();
            b.categoryProgress.setProgressCompat(pct, true);
            b.tvProgressPercent.setText(String.format(Locale.getDefault(), "%.2f%%",
                item.budgetLimit == 0 ? 0.0 : (item.spentAmount / item.budgetLimit) * 100));

            boolean isOver = item.spentAmount > item.budgetLimit;

            int barColor = isOver
                ? ctx.getColor(com.mopr.personal_finance_manager.R.color.expense_red)
                : ctx.getColor(com.mopr.personal_finance_manager.R.color.budget_yellow_accent);

            b.categoryProgress.setIndicatorColor(barColor);

            if (isOver) {
                double over = item.spentAmount - item.budgetLimit;
                b.tvRemainingAmount.setText(
                    CurrencyFormatter.formatVND(over) + " Over");
                b.tvRemainingAmount.setTextColor(
                    ctx.getColor(com.mopr.personal_finance_manager.R.color.expense_red));
            } else {
                b.tvRemainingAmount.setText(
                    CurrencyFormatter.formatVND(item.getRemaining()) + " Left");
                b.tvRemainingAmount.setTextColor(
                    ctx.getColor(com.mopr.personal_finance_manager.R.color.budget_yellow_accent));
            }
        }
    }
}

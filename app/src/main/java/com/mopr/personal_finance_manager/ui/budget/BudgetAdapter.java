package com.mopr.personal_finance_manager.ui.budget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Budget;
import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.databinding.ItemCategoryBudgetBinding;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }

    private List<BudgetUIItem> items = new ArrayList<>();
    private final OnBudgetClickListener listener;

    public BudgetAdapter(OnBudgetClickListener listener) {
        this.listener = listener;
    }

    public static class BudgetUIItem {
        public Budget budget;
        public double spentAmount;

        public BudgetUIItem(Budget budget, double spentAmount) {
            this.budget = budget;
            this.spentAmount = spentAmount;
        }

        public int getProgress() {
            if (budget.limitAmount <= 0) return 0;
            return (int) Math.min(100, (spentAmount / budget.limitAmount) * 100);
        }

        public double getRemaining() {
            return Math.max(0, budget.limitAmount - spentAmount);
        }
    }

    public void setItems(List<BudgetUIItem> newItems) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return items.size(); }
            @Override
            public int getNewListSize() { return newItems.size(); }
            @Override
            public boolean areItemsTheSame(int o, int n) {
                return items.get(o).budget.id == newItems.get(n).budget.id;
            }
            @Override
            public boolean areContentsTheSame(int o, int n) {
                BudgetUIItem oldI = items.get(o), newI = newItems.get(n);
                return oldI.budget.limitAmount == newI.budget.limitAmount &&
                       oldI.spentAmount == newI.spentAmount &&
                       oldI.budget.category.equals(newI.budget.category);
            }
        });
        this.items = new ArrayList<>(newItems);
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
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBudgetBinding b;

        ViewHolder(ItemCategoryBudgetBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(BudgetUIItem item, OnBudgetClickListener listener) {
            Context ctx = itemView.getContext();
            Budget budget = item.budget;

            b.tvCategoryName.setText(Category.getDisplayName(ctx, budget.category));
            int iconRes = Category.getIconRes(budget.category);
            int colorRes = Category.getColorRes(budget.category);
            b.ivCategoryIcon.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_cat_other);
            b.ivCategoryIcon.setBackgroundTintList(ColorStateList.valueOf(
                ctx.getColor(colorRes != 0 ? colorRes : R.color.cat_other)));

            b.tvSpentAmount.setText(CurrencyFormatter.formatVND(item.spentAmount));
            b.tvBudgetAmount.setText(CurrencyFormatter.formatVND(budget.limitAmount));

            int progress = item.getProgress();
            android.widget.LinearLayout.LayoutParams lp =
                (android.widget.LinearLayout.LayoutParams) b.categoryProgressIndicator.getLayoutParams();
            lp.weight = (float) Math.min(100, progress);
            b.categoryProgressIndicator.setLayoutParams(lp);
            b.tvProgressPercent.setText(String.format(Locale.getDefault(), "%.1f%%",
                budget.limitAmount == 0 ? 0 : (item.spentAmount / budget.limitAmount) * 100));

            boolean isOver = item.spentAmount > budget.limitAmount;
            int accentColor = isOver ? ctx.getColor(R.color.expense_red) : ctx.getColor(R.color.budget_yellow_accent);

            b.categoryProgressIndicator.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            b.tvRemainingAmount.setTextColor(accentColor);

            if (isOver) {
                b.tvRemainingAmount.setText(CurrencyFormatter.formatVND(item.spentAmount - budget.limitAmount) + " Over");
            } else {
                b.tvRemainingAmount.setText(CurrencyFormatter.formatVND(item.getRemaining()) + " Left");
            }

            b.btnAddTransaction.setOnClickListener(v -> listener.onBudgetClick(budget));
        }
    }
}

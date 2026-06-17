package com.mopr.personal_finance_manager.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

    public void setItems(List<CategoryBudgetUI> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBudgetBinding binding = ItemCategoryBudgetBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
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
        private final ItemCategoryBudgetBinding binding;

        ViewHolder(ItemCategoryBudgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoryBudgetUI item) {
            Context context = itemView.getContext();
            binding.tvCategoryName.setText(Category.getDisplayName(context, item.category));
            binding.ivCategoryIcon.setImageResource(Category.getIconRes(item.category));
            binding.ivCategoryIcon.setBackgroundColor(context.getColor(Category.getColorRes(item.category)));

            binding.tvSpentAmount.setText(CurrencyFormatter.formatVND(item.spentAmount));
            binding.tvBudgetAmount.setText(CurrencyFormatter.formatVND(item.budgetLimit));
            binding.tvRemainingAmount.setText(CurrencyFormatter.formatVND(item.getRemaining()) + " Left");

            binding.categoryProgress.setProgress(item.getProgress());
            double percent = item.budgetLimit == 0 ? 0 : (item.spentAmount / item.budgetLimit) * 100;
            binding.tvProgressPercent.setText(String.format(Locale.getDefault(), "%.2f%%", percent));

            if (item.spentAmount > item.budgetLimit) {
                binding.tvRemainingAmount.setTextColor(context.getColor(android.R.color.holo_red_dark));
                binding.categoryProgress.setIndicatorColor(context.getColor(android.R.color.holo_red_dark));
                binding.tvRemainingAmount.setText(CurrencyFormatter.formatVND(item.spentAmount - item.budgetLimit) + " Over");
            } else {
                binding.tvRemainingAmount.setTextColor(context.getColor(com.mopr.personal_finance_manager.R.color.budget_yellow_accent));
                binding.categoryProgress.setIndicatorColor(context.getColor(com.mopr.personal_finance_manager.R.color.budget_yellow_accent));
                binding.tvRemainingAmount.setText(CurrencyFormatter.formatVND(item.getRemaining()) + " Left");
            }
        }
    }
}

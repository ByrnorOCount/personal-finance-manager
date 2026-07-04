package com.mopr.personal_finance_manager.ui.budget;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.CategoryBudget;
import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.databinding.ItemPlanningBudgetBinding;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class PlanningBudgetAdapter extends RecyclerView.Adapter<PlanningBudgetAdapter.ViewHolder> {

    private List<CategoryBudget> items = new ArrayList<>();
    private OnBudgetChangeListener listener;

    public interface OnBudgetChangeListener {
        void onBudgetChanged();
        void onRemoveBudget(CategoryBudget budget);
        void onEditBudget(CategoryBudget budget);
    }

    public PlanningBudgetAdapter(OnBudgetChangeListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CategoryBudget> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPlanningBudgetBinding.inflate(
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlanningBudgetBinding b;

        ViewHolder(ItemPlanningBudgetBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(CategoryBudget item) {
            b.tvCategoryName.setText(Category.getDisplayName(itemView.getContext(), item.category));
            b.tvCategoryLimit.setText(CurrencyFormatter.formatVND(item.limitAmount));

            int accentColor;
            if ("INCOME".equals(item.type)) {
                accentColor = itemView.getContext().getColor(R.color.income_green_accent);
            } else {
                accentColor = itemView.getContext().getColor(R.color.provisional_blue);
            }
            b.tvCategoryLimit.setTextColor(accentColor);
            b.seekBarLimit.setThumbTintList(android.content.res.ColorStateList.valueOf(accentColor));
            b.seekBarLimit.setProgressTintList(android.content.res.ColorStateList.valueOf(accentColor));

            b.seekBarLimit.setProgress((int) item.limitAmount);
            b.seekBarLimit.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        item.limitAmount = progress;
                        b.tvCategoryLimit.setText(CurrencyFormatter.formatVND(item.limitAmount));
                        if (listener != null) listener.onBudgetChanged();
                    }
                }
                @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
            });

            b.btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveBudget(item);
            });

            b.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditBudget(item);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEditBudget(item);
            });
        }
    }
}

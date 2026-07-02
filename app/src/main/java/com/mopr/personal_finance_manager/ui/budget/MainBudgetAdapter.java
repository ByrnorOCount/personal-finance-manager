package com.mopr.personal_finance_manager.ui.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mopr.personal_finance_manager.data.local.MainBudget;
import com.mopr.personal_finance_manager.databinding.ItemMainBudgetBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainBudgetAdapter extends RecyclerView.Adapter<MainBudgetAdapter.ViewHolder> {

    private List<MainBudget> items = new ArrayList<>();
    private OnBudgetClickListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnBudgetClickListener {
        void onBudgetClick(MainBudget budget);
        void onDeleteClick(MainBudget budget);
    }

    public MainBudgetAdapter(OnBudgetClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<MainBudget> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMainBudgetBinding.inflate(
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
        private final ItemMainBudgetBinding b;

        ViewHolder(ItemMainBudgetBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(MainBudget item) {
            b.tvBudgetName.setText(item.name);
            b.tvBudgetDates.setText(sdf.format(item.startDate) + " - " + sdf.format(item.endDate));
            b.ivActiveIndicator.setVisibility(item.isActive ? View.VISIBLE : View.GONE);

            b.btnDeleteBudget.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onBudgetClick(item);
            });
        }
    }
}

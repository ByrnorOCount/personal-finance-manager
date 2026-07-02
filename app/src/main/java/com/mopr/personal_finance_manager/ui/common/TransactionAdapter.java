package com.mopr.personal_finance_manager.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.local.TransactionWithCategory;
import com.mopr.personal_finance_manager.databinding.ItemTransactionBinding;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionWithCategory> items = new ArrayList<>();
    private OnTransactionClickListener listener;

    public void setListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    /**
     * DiffUtil-powered update — avoids full notifyDataSetChanged flicker.
     */
    public void setTransactions(List<TransactionWithCategory> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return items.get(oldPos).transaction.id == newList.get(newPos).transaction.id;
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                TransactionWithCategory o = items.get(oldPos);
                TransactionWithCategory n = newList.get(newPos);
                return o.transaction.amount == n.transaction.amount
                    && o.transaction.categoryId == n.transaction.categoryId
                    && o.transaction.type.equals(n.transaction.type)
                    && o.transaction.date == n.transaction.date
                    && ((o.transaction.note == null && n.transaction.note == null)
                    || (o.transaction.note != null && o.transaction.note.equals(n.transaction.note)));
            }
        });
        items = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }

    public TransactionWithCategory getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionWithCategory item = items.get(position);
        com.mopr.personal_finance_manager.data.local.Transaction t = item.transaction;
        com.mopr.personal_finance_manager.data.local.Category cat = item.category;

        String catName = cat != null ? cat.name : "Unknown";
        int iconRes = (cat != null && cat.iconRes != 0) ? cat.iconRes : R.drawable.ic_cat_other;
        int colorRes = (cat != null && cat.colorRes != 0) ? cat.colorRes : R.color.cat_other;

        holder.binding.tvCategory.setText(catName);
        holder.binding.ivIcon.setImageResource(iconRes);

        // Category icon with colored filling
        int catColor = holder.itemView.getContext().getColor(colorRes);
        holder.binding.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(catColor));
        holder.binding.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));

        String note = (t.note != null && !t.note.isEmpty()) ? t.note : catName;
        holder.binding.tvNote.setText(note);
        holder.binding.tvDate.setText(DateUtils.formatDate(t.date));

        // Amount with sign and colour
        if (t.isExpense()) {
            holder.binding.tvAmount.setText("- " + CurrencyFormatter.formatVND(t.amount));
            holder.binding.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.expense_red));
        } else {
            holder.binding.tvAmount.setText("+ " + CurrencyFormatter.formatVND(t.amount));
            holder.binding.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.income_green));
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(item, holder.getAdapterPosition());
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onTransactionLongClick(item, holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionWithCategory item, int position);

        void onTransactionLongClick(TransactionWithCategory item, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        ViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

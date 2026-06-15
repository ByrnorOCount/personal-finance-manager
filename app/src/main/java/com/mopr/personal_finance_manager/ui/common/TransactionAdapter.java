package com.mopr.personal_finance_manager.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mopr.personal_finance_manager.R;
import com.mopr.personal_finance_manager.data.local.Transaction;
import com.mopr.personal_finance_manager.data.model.Category;
import com.mopr.personal_finance_manager.databinding.ItemTransactionBinding;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private OnTransactionClickListener listener;

    public void setListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    /**
     * DiffUtil-powered update — avoids full notifyDataSetChanged flicker.
     */
    public void setTransactions(List<Transaction> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return transactions.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return transactions.get(oldPos).id == newList.get(newPos).id;
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Transaction o = transactions.get(oldPos);
                Transaction n = newList.get(newPos);
                return o.amount == n.amount
                    && o.category.equals(n.category)
                    && o.type.equals(n.type)
                    && o.date == n.date
                    && ((o.note == null && n.note == null)
                    || (o.note != null && o.note.equals(n.note)));
            }
        });
        transactions = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }

    public Transaction getItem(int position) {
        return transactions.get(position);
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
        Transaction t = transactions.get(position);
        holder.binding.tvCategory.setText(Category.getDisplayName(holder.itemView.getContext(), t.category));
        holder.binding.ivIcon.setImageResource(Category.getIconRes(t.category));

        // Category icon with colored filling
        int catColor = holder.itemView.getContext().getColor(Category.getColorRes(t.category));
        holder.binding.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(catColor));
        holder.binding.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));

        String note = (t.note != null && !t.note.isEmpty()) ? t.note : Category.getDisplayName(holder.itemView.getContext(), t.category);
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
            if (listener != null) listener.onTransactionClick(t, holder.getAdapterPosition());
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onTransactionLongClick(t, holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction, int position);

        void onTransactionLongClick(Transaction transaction, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        ViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

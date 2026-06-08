package com.mopr.personal_finance_manager.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mopr.personal_finance_manager.data.local.entity.Category;
import com.mopr.personal_finance_manager.data.local.entity.Transaction;
import com.mopr.personal_finance_manager.databinding.ItemTransactionBinding;
import com.mopr.personal_finance_manager.util.CurrencyFormatter;
import com.mopr.personal_finance_manager.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
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
        Transaction transaction = transactions.get(position);
        holder.binding.tvCategory.setText(Category.getDisplayName(transaction.category));
        holder.binding.tvIcon.setText(Category.getIcon(transaction.category));
        holder.binding.tvNote.setText(transaction.note);
        holder.binding.tvDate.setText(DateUtils.formatDate(transaction.date));

        String amountPrefix = transaction.isExpense() ? "- " : "+ ";
        holder.binding.tvAmount.setText(amountPrefix + CurrencyFormatter.formatVND(transaction.amount));
        holder.binding.tvAmount.setTextColor(transaction.isExpense() ? Color.RED : Color.parseColor("#2D6A4F"));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        ViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

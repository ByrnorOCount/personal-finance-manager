package com.mopr.personal_finance_manager.ui.budget;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mopr.personal_finance_manager.data.local.Category;
import com.mopr.personal_finance_manager.databinding.ItemSubcategorySmallBinding;
import java.util.ArrayList;
import java.util.List;

public class SubcategorySmallAdapter extends RecyclerView.Adapter<SubcategorySmallAdapter.ViewHolder> {

    public interface OnSubcategoryActionListener {
        void onRemove(Category subcategory);
    }

    private List<Category> items = new ArrayList<>();
    private final OnSubcategoryActionListener listener;

    public SubcategorySmallAdapter(OnSubcategoryActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Category> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSubcategorySmallBinding.inflate(
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
        private final ItemSubcategorySmallBinding b;

        ViewHolder(ItemSubcategorySmallBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Category item) {
            b.tvSubcategoryName.setText(item.name);
            b.btnRemoveSub.setOnClickListener(v -> {
                if (listener != null) listener.onRemove(item);
            });
        }
    }
}

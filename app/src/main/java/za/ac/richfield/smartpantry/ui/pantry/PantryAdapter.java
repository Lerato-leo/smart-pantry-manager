package za.ac.richfield.smartpantry.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.model.PantryItem;

import java.util.List;

/**
 * Adapter for displaying pantry items in a RecyclerView.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private List<PantryItem> pantryItems;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener; // We'll use long click for edit, or we can use regular click

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(PantryItem item);
    }

    public PantryAdapter(List<PantryItem> pantryItems, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.pantryItems = pantryItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return pantryItems == null ? 0 : pantryItems.size();
    }

    public void setPantryItems(List<PantryItem> pantryItems) {
        this.pantryItems = pantryItems;
        notifyDataSetChanged();
    }

    public class PantryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvQuantityUnit;
        private final TextView tvExpiryDate;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQuantityUnit = itemView.findViewById(R.id.tv_item_quantity_unit);
            tvExpiryDate = itemView.findViewById(R.id.tv_item_expiry_date);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(pantryItems.get(pos));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(pantryItems.get(pos));
                    return true;
                }
                return false;
            });
        }

        public void bind(PantryItem item) {
            tvName.setText(item.getName());
            String quantityUnit = String.format("%s %s", item.getQuantity(), item.getUnit());
            tvQuantityUnit.setText(quantityUnit);
            String expiryDate = item.getExpiryDate();
            if (expiryDate != null && !expiryDate.isEmpty()) {
                tvExpiryDate.setText(expiresOn(expiryDate));
                tvExpiryDate.setVisibility(View.VISIBLE);
            } else {
                tvExpiryDate.setVisibility(View.GONE);
            }
        }

        private String expiresOn(String date) {
            // We can format the date to be more user-friendly, but for now just show the date.
            return "Expires: " + date;
        }
    }
}
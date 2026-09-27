package za.ac.richfield.spens.ui.pantry;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import za.ac.richfield.spens.R;
import za.ac.richfield.spens.data.entity.PantryItem;
import za.ac.richfield.spens.util.ExpiryDateConverter;
import za.ac.richfield.spens.util.QuantityFormatter;

/**
 * Adapter for displaying pantry items in a RecyclerView.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.PantryViewHolder> {

    private final OnItemClickListener clickListener;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(PantryItem item);
    }

    public PantryAdapter(OnItemClickListener clickListener, OnDeleteClickListener deleteClickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.deleteClickListener = deleteClickListener;
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<PantryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getQuantity() == newItem.getQuantity()
                    && Objects.equals(oldItem.getUnit(), newItem.getUnit())
                    && Objects.equals(oldItem.getExpiryDate(), newItem.getExpiryDate());
        }
    };

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class PantryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvQuantityUnit;
        private final TextView tvExpiryDate;
        private final View statusDot;
        private final ImageButton btnDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQuantityUnit = itemView.findViewById(R.id.tv_item_quantity_unit);
            tvExpiryDate = itemView.findViewById(R.id.tv_item_expiry_date);
            statusDot = itemView.findViewById(R.id.view_status_dot);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(getItem(pos));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(getItem(pos));
                }
            });
        }

        public void bind(PantryItem item) {
            tvName.setText(item.getName());
            btnDelete.setContentDescription(itemView.getContext().getString(
                    R.string.content_desc_delete_item, item.getName()));
            tvQuantityUnit.setText(QuantityFormatter.formatWithUnit(item.getQuantity(), item.getUnit()));

            Long expiryDate = item.getExpiryDate();
            if (expiryDate == null) {
                tvExpiryDate.setVisibility(View.GONE);
                // INVISIBLE (not GONE) keeps the dot's width reserved, so the name column
                // lines up whether or not a given card shows an expiry status.
                statusDot.setVisibility(View.INVISIBLE);
                return;
            }

            tvExpiryDate.setVisibility(View.VISIBLE);
            statusDot.setVisibility(View.VISIBLE);
            tvExpiryDate.setText(itemView.getContext().getString(
                    R.string.format_expires_on, ExpiryDateConverter.formatEpochDay(expiryDate)));

            int statusColorRes = statusColorFor(expiryDate);
            int statusColor = ContextCompat.getColor(itemView.getContext(), statusColorRes);
            tvExpiryDate.setTextColor(statusColor);
            statusDot.setBackgroundTintList(ColorStateList.valueOf(statusColor));
        }

        private int statusColorFor(long expiryEpochDay) {
            long today = ExpiryDateConverter.todayEpochDay();
            if (expiryEpochDay < today) {
                return R.color.status_overdue;
            }
            if (expiryEpochDay <= today + ExpiryDateConverter.EXPIRY_SOON_WINDOW_DAYS) {
                return R.color.status_soon;
            }
            return R.color.status_fresh;
        }
    }
}

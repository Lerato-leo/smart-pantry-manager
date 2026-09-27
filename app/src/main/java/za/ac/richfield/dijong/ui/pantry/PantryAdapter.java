package za.ac.richfield.dijong.ui.pantry;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import za.ac.richfield.dijong.R;
import za.ac.richfield.dijong.data.IngredientCategory;
import za.ac.richfield.dijong.data.entity.PantryItem;
import za.ac.richfield.dijong.ui.ExpiryBadge;
import za.ac.richfield.dijong.util.ExpiryStatus;
import za.ac.richfield.dijong.util.QuantityFormatter;

/**
 * Binds pantry items to cards: a category-coloured icon, the name, the quantity with a
 * category chip, and an expiry badge for anything that needs using up.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.PantryViewHolder> {

    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    public PantryAdapter(OnItemClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
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
                    && Objects.equals(oldItem.getExpiryDate(), newItem.getExpiryDate())
                    && oldItem.getCategory().equals(newItem.getCategory());
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
        private final View iconCircle;
        private final ImageView ivCategoryIcon;
        private final TextView tvName;
        private final TextView tvQuantityUnit;
        private final TextView tvCategoryChip;
        private final TextView tvExpiryBadge;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCircle = itemView.findViewById(R.id.icon_circle);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQuantityUnit = itemView.findViewById(R.id.tv_item_quantity_unit);
            tvCategoryChip = itemView.findViewById(R.id.tv_category_chip);
            tvExpiryBadge = itemView.findViewById(R.id.tv_expiry_badge);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(getItem(pos));
                }
            });
        }

        public void bind(PantryItem item) {
            Context context = itemView.getContext();
            tvName.setText(item.getName());
            tvQuantityUnit.setText(QuantityFormatter.formatWithUnit(item.getQuantity(), item.getUnit()));

            IngredientCategory category = item.getCategoryEnum();
            ColorStateList background = ColorStateList.valueOf(ContextCompat.getColor(context, category.backgroundColorRes));
            int foreground = ContextCompat.getColor(context, category.foregroundColorRes);
            iconCircle.setBackgroundTintList(background);
            ivCategoryIcon.setImageResource(category.iconRes);
            ivCategoryIcon.setImageTintList(ColorStateList.valueOf(foreground));
            tvCategoryChip.setText(category.labelRes);
            tvCategoryChip.setBackgroundTintList(background);
            tvCategoryChip.setTextColor(foreground);

            ExpiryBadge.bind(tvExpiryBadge, ExpiryStatus.of(item.getExpiryDate()), false);
        }
    }
}

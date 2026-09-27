package za.ac.richfield.dijong.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import za.ac.richfield.dijong.R;
import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;
import za.ac.richfield.dijong.util.QuantityFormatter;
import za.ac.richfield.dijong.util.RecipeMatcher.AlmostThereRecipe;

/**
 * Adapter for the "Almost there" list: recipes one ingredient short, each showing what's
 * missing ("Missing 500 g maize meal") or how much more is needed ("Need 200 g more flour").
 */
public class AlmostThereAdapter extends ListAdapter<AlmostThereRecipe, AlmostThereAdapter.AlmostThereViewHolder> {

    private final RecipeAdapter.OnRecipeClickListener clickListener;

    public AlmostThereAdapter(RecipeAdapter.OnRecipeClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<AlmostThereRecipe> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AlmostThereRecipe>() {
                @Override
                public boolean areItemsTheSame(@NonNull AlmostThereRecipe oldItem, @NonNull AlmostThereRecipe newItem) {
                    return oldItem.getRecipe().getId() == newItem.getRecipe().getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull AlmostThereRecipe oldItem, @NonNull AlmostThereRecipe newItem) {
                    // The missing line changes as stock changes, even when the recipe doesn't.
                    return oldItem.getMissingIngredient().getId() == newItem.getMissingIngredient().getId()
                            && oldItem.getMissingQuantity() == newItem.getMissingQuantity();
                }
            };

    @NonNull
    @Override
    public AlmostThereViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_almost_there, parent, false);
        return new AlmostThereViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlmostThereViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class AlmostThereViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvMissing;

        public AlmostThereViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvMissing = itemView.findViewById(R.id.tv_missing_ingredient);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onRecipeClick(getItem(pos).getRecipe());
                }
            });
        }

        public void bind(AlmostThereRecipe almostThere) {
            RecipeWithIngredients recipe = almostThere.getRecipe();
            RecipeIngredient missing = almostThere.getMissingIngredient();
            tvName.setText(recipe.getName());

            String amount = QuantityFormatter.formatWithUnit(almostThere.getMissingQuantity(), missing.getUnit());
            int format = almostThere.isMissingEntirely()
                    ? R.string.format_missing_ingredient
                    : R.string.format_need_more_ingredient;
            tvMissing.setText(itemView.getContext().getString(format, amount, missing.getIngredientName().toLowerCase()));
        }
    }
}

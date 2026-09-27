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

/**
 * Adapter for displaying recipes in a RecyclerView.
 */
public class RecipeAdapter extends ListAdapter<RecipeWithIngredients, RecipeAdapter.RecipeViewHolder> {

    private final OnRecipeClickListener clickListener;

    public interface OnRecipeClickListener {
        void onRecipeClick(RecipeWithIngredients recipe);
    }

    public RecipeAdapter(OnRecipeClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<RecipeWithIngredients> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RecipeWithIngredients>() {
                @Override
                public boolean areItemsTheSame(@NonNull RecipeWithIngredients oldItem, @NonNull RecipeWithIngredients newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull RecipeWithIngredients oldItem, @NonNull RecipeWithIngredients newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getIngredients().size() == newItem.getIngredients().size();
                }
            };

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvIngredientCount;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvIngredientCount = itemView.findViewById(R.id.tv_ingredient_count);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onRecipeClick(getItem(pos));
                }
            });
        }

        public void bind(RecipeWithIngredients recipe) {
            tvName.setText(recipe.getName());
            int count = recipe.getIngredients().size();
            tvIngredientCount.setText(itemView.getResources()
                    .getQuantityString(R.plurals.recipe_ingredient_count, count, count));
        }
    }
}

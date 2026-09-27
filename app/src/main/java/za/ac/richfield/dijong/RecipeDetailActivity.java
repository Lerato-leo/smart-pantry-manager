package za.ac.richfield.dijong;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.Map;

import za.ac.richfield.dijong.data.AppSettings;
import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;
import za.ac.richfield.dijong.ui.recipes.RecipeDetailViewModel;
import za.ac.richfield.dijong.util.QuantityFormatter;
import za.ac.richfield.dijong.util.RecipeMatcher;
import za.ac.richfield.dijong.util.RecipeSteps;
import za.ac.richfield.dijong.util.UnitSystem;

/**
 * Shows one recipe: whether you can cook it, each ingredient ticked or marked missing
 * against your pantry, and the method as numbered steps. Opened from What Can I Cook with
 * the recipe's id in {@link #EXTRA_RECIPE_ID}.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    private static final long NO_RECIPE_ID = -1;

    private TextView tvRecipeName;
    private TextView tvIngredientCount;
    private TextView tvStatusBadge;
    private LinearLayout ingredientsContainer;
    private LinearLayout stepsContainer;

    private RecipeWithIngredients recipe;
    private Map<String, Double> pantryStock;
    private boolean imperial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvRecipeName = findViewById(R.id.tv_recipe_name);
        tvIngredientCount = findViewById(R.id.tv_ingredient_count);
        tvStatusBadge = findViewById(R.id.tv_status_badge);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        stepsContainer = findViewById(R.id.steps_container);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.title_recipe);
        imperial = new AppSettings(this).isImperialUnitsEnabled();

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, NO_RECIPE_ID);
        if (recipeId == NO_RECIPE_ID) {
            finish();
            return;
        }

        RecipeDetailViewModel viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);
        viewModel.setRecipeId(recipeId);
        viewModel.getRecipe().observe(this, loaded -> {
            recipe = loaded;
            render();
        });
        viewModel.getPantryStock().observe(this, stock -> {
            pantryStock = stock;
            render();
        });
    }

    /** Draws the screen once both the recipe and the pantry totals have arrived. */
    private void render() {
        if (recipe == null || pantryStock == null) {
            return;
        }
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        tvRecipeName.setText(recipe.getName());
        tvIngredientCount.setText(getResources().getQuantityString(
                R.plurals.recipe_ingredient_count, ingredients.size(), ingredients.size()));

        ingredientsContainer.removeAllViews();
        int missingCount = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (RecipeIngredient ingredient : ingredients) {
            boolean inStock = RecipeMatcher.isInStock(ingredient, pantryStock);
            if (!inStock) {
                missingCount++;
            }
            ingredientsContainer.addView(ingredientRow(inflater, ingredientsContainer, ingredient, inStock));
        }
        showStatusBadge(missingCount);

        stepsContainer.removeAllViews();
        List<String> steps = RecipeSteps.split(recipe.getPrepSteps());
        for (int i = 0; i < steps.size(); i++) {
            View row = inflater.inflate(R.layout.item_method_step, stepsContainer, false);
            ((TextView) row.findViewById(R.id.tv_step_number)).setText(String.valueOf(i + 1));
            ((TextView) row.findViewById(R.id.tv_step_text)).setText(steps.get(i));
            stepsContainer.addView(row);
        }
    }

    private View ingredientRow(LayoutInflater inflater, ViewGroup parent, RecipeIngredient ingredient, boolean inStock) {
        View row = inflater.inflate(R.layout.item_detail_ingredient, parent, false);
        View statusCircle = row.findViewById(R.id.status_circle);
        ImageView statusIcon = row.findViewById(R.id.iv_status);
        if (inStock) {
            statusCircle.setContentDescription(getString(R.string.content_desc_in_pantry));
        } else {
            statusCircle.setBackgroundResource(R.drawable.bg_circle_ring);
            statusCircle.setBackgroundTintList(null);
            statusIcon.setImageResource(R.drawable.ic_close);
            statusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.ink)));
            statusIcon.getLayoutParams().width = statusIcon.getLayoutParams().height =
                    getResources().getDimensionPixelSize(R.dimen.missing_icon_size);
            statusCircle.setContentDescription(getString(R.string.content_desc_not_in_pantry));
            row.findViewById(R.id.tv_missing_pill).setVisibility(View.VISIBLE);
        }
        ((TextView) row.findViewById(R.id.tv_ingredient_name)).setText(capitalise(ingredient.getIngredientName()));
        UnitSystem.Amount amount = UnitSystem.forDisplay(ingredient.getRequiredQuantity(), ingredient.getUnit(), imperial);
        ((TextView) row.findViewById(R.id.tv_ingredient_amount)).setText(
                QuantityFormatter.formatRecipeAmount(amount.quantity, amount.unit));
        return row;
    }

    private void showStatusBadge(int missingCount) {
        boolean ready = missingCount == 0;
        tvStatusBadge.setText(ready
                ? getString(R.string.you_have_everything)
                : getResources().getQuantityString(R.plurals.missing_ingredient_count, missingCount, missingCount));
        tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, ready ? R.color.teal : R.color.amber)));
        tvStatusBadge.setTextColor(ContextCompat.getColor(this, ready ? R.color.white : R.color.ink));
    }

    /** Seeded ingredient names are lower case ("curry powder"); lists read better as "Curry powder". */
    private static String capitalise(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

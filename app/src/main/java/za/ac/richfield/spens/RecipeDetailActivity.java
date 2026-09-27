package za.ac.richfield.spens;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import za.ac.richfield.spens.data.RecipeWithIngredients;
import za.ac.richfield.spens.data.entity.RecipeIngredient;
import za.ac.richfield.spens.ui.recipes.RecipeDetailViewModel;
import za.ac.richfield.spens.util.QuantityFormatter;

/**
 * Activity to display a recipe's details.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    private static final long NO_RECIPE_ID = -1;

    private TextView tvRecipeName;
    private TextView tvIngredientsList;
    private TextView tvPrepSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvRecipeName = findViewById(R.id.tv_recipe_name);
        tvIngredientsList = findViewById(R.id.tv_ingredients_list);
        tvPrepSteps = findViewById(R.id.tv_prep_steps);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, NO_RECIPE_ID);
        if (recipeId == NO_RECIPE_ID) {
            finish();
            return;
        }

        RecipeDetailViewModel viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);
        viewModel.setRecipeId(recipeId);
        viewModel.getRecipe().observe(this, this::showRecipe);
    }

    private void showRecipe(RecipeWithIngredients recipe) {
        if (recipe == null) {
            return;
        }
        tvRecipeName.setText(recipe.getName());

        StringBuilder ingredientsBuilder = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredientsBuilder.append("• ")
                    .append(QuantityFormatter.formatWithUnit(ingredient.getRequiredQuantity(), ingredient.getUnit()))
                    .append(" ")
                    .append(ingredient.getIngredientName())
                    .append("\n");
        }
        tvIngredientsList.setText(ingredientsBuilder.toString());
        tvPrepSteps.setText(recipe.getPrepSteps());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

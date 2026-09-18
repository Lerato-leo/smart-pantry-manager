package za.ac.richfield.smartpantry;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;
import za.ac.richfield.smartpantry.db.DatabaseHelper;
import za.ac.richfield.smartpantry.model.Recipe;

/**
 * Activity to display a recipe's details.
 * Demonstrates Intent usage for receiving the recipe ID.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private TextView tvRecipeName;
    private TextView tvIngredientsList;
    private TextView tvPrepSteps;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvRecipeName = findViewById(R.id.tv_recipe_name);
        tvIngredientsList = findViewById(R.id.tv_ingredients_list);
        tvPrepSteps = findViewById(R.id.tv_prep_steps);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get recipe ID from intent
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId != -1) {
            loadRecipe(recipeId);
        } else {
            // If no recipe ID, finish the activity
            finish();
        }
    }

    private void loadRecipe(long recipeId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Recipe recipe = dbHelper.getRecipe(recipeId);
        dbHelper.close(); // Close the database helper

        if (recipe != null) {
            tvRecipeName.setText(recipe.getName());

            // Build ingredients list
            StringBuilder ingredientsBuilder = new StringBuilder();
            for (za.ac.richfield.smartpantry.model.RecipeIngredient ingredient : recipe.getIngredients()) {
                ingredientsBuilder.append("• ")
                        .append(ingredient.getQuantity())
                        .append(" ")
                        .append(ingredient.getUnit())
                        .append(" ")
                        .append(ingredient.getIngredientName())
                        .append("\n");
            }
            tvIngredientsList.setText(ingredientsBuilder.toString());

            tvPrepSteps.setText(recipe.getPrepSteps());
        } else {
            // Recipe not found
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
package za.ac.richfield.dijong.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import za.ac.richfield.dijong.data.dao.RecipeDao;

/**
 * Read-only access to the recipe catalogue seeded by {@link SouthAfricanRecipeSeeder}. There's
 * no write path here on purpose: recipes ship with the app rather than being user-editable, so
 * unlike {@link PantryRepository} there's nothing to hand off to a background executor.
 */
public class RecipeRepository {

    private final RecipeDao recipeDao;

    public RecipeRepository(Application application) {
        recipeDao = DijongDatabase.getInstance(application).recipeDao();
    }

    public LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients() {
        return recipeDao.getAllRecipesWithIngredients();
    }

    public LiveData<RecipeWithIngredients> getRecipeWithIngredients(long id) {
        return recipeDao.getRecipeWithIngredients(id);
    }
}

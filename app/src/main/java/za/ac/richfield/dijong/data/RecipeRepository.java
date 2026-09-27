package za.ac.richfield.dijong.data;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;

import za.ac.richfield.dijong.data.dao.RecipeDao;

/**
 * Read-only access to the recipe catalogue seeded by {@link SouthAfricanRecipeSeeder}. There's
 * no write path here on purpose: recipes ship with the app rather than being user-editable, so
 * unlike {@link PantryRepository} there's nothing to hand off to a background executor.
 */
public class RecipeRepository {

    private final DijongDatabase database;
    private final RecipeDao recipeDao;
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public RecipeRepository(Application application) {
        database = DijongDatabase.getInstance(application);
        recipeDao = database.recipeDao();
    }

    public LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients() {
        return recipeDao.getAllRecipesWithIngredients();
    }

    public LiveData<RecipeWithIngredients> getRecipeWithIngredients(long id) {
        return recipeDao.getRecipeWithIngredients(id);
    }

    /**
     * Puts the built-in cookbook back exactly as it was on first launch. Pantry items are
     * untouched. Runs as one transaction, so a screen watching the recipes never sees a
     * half-empty catalogue; {@code onDone} runs on the main thread afterwards.
     */
    public void resetSampleRecipes(Runnable onDone) {
        DijongDatabase.databaseWriteExecutor.execute(() -> {
            database.resetSampleRecipes();
            mainThread.post(onDone);
        });
    }
}

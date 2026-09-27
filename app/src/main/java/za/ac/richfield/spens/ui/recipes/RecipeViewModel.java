package za.ac.richfield.spens.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import za.ac.richfield.spens.data.PantryRepository;
import za.ac.richfield.spens.data.RecipeRepository;
import za.ac.richfield.spens.data.RecipeWithIngredients;
import za.ac.richfield.spens.data.entity.PantryItem;
import za.ac.richfield.spens.util.RecipeMatcher;

/**
 * Recomputes the list of recipes the pantry can currently make whenever either the
 * recipe catalog or the pantry contents change.
 */
public class RecipeViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<RecipeWithIngredients>> matchingRecipes = new MediatorLiveData<>();

    private List<RecipeWithIngredients> latestRecipes = Collections.emptyList();
    private List<PantryItem> latestPantryItems = Collections.emptyList();

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        RecipeRepository recipeRepository = new RecipeRepository(application);
        PantryRepository pantryRepository = new PantryRepository(application);

        matchingRecipes.addSource(recipeRepository.getAllRecipesWithIngredients(), recipes -> {
            latestRecipes = recipes != null ? recipes : Collections.emptyList();
            recompute();
        });
        matchingRecipes.addSource(pantryRepository.getAllItems(), items -> {
            latestPantryItems = items != null ? items : Collections.emptyList();
            recompute();
        });
    }

    private void recompute() {
        Map<String, Double> pantryQuantities = RecipeMatcher.buildPantryQuantityMap(latestPantryItems);
        matchingRecipes.setValue(RecipeMatcher.getMatchingRecipes(latestRecipes, pantryQuantities));
    }

    public LiveData<List<RecipeWithIngredients>> getMatchingRecipes() {
        return matchingRecipes;
    }
}

package za.ac.richfield.dijong.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import za.ac.richfield.dijong.data.PantryRepository;
import za.ac.richfield.dijong.data.RecipeRepository;
import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.data.entity.PantryItem;
import za.ac.richfield.dijong.util.ExpiryDateConverter;
import za.ac.richfield.dijong.util.RecipeMatcher;
import za.ac.richfield.dijong.util.RecipeMatcher.AlmostThereRecipe;

/**
 * Recomputes the list of recipes the pantry can currently make, and the separate
 * "almost there" list of recipes one ingredient short, whenever either the recipe
 * catalog or the pantry contents change.
 */
public class RecipeViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<RecipeWithIngredients>> matchingRecipes = new MediatorLiveData<>();
    private final MutableLiveData<List<AlmostThereRecipe>> almostThereRecipes = new MutableLiveData<>();
    private final MutableLiveData<Integer> pantryItemCount = new MutableLiveData<>(0);

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
        pantryItemCount.setValue(latestPantryItems.size());
        // Only food that hasn't expired can make a recipe ready
        Map<String, Double> pantryQuantities = RecipeMatcher.buildPantryQuantityMap(
                latestPantryItems, ExpiryDateConverter.todayEpochDay());
        Map<String, Double> allQuantities = RecipeMatcher.buildPantryQuantityMap(latestPantryItems);
        almostThereRecipes.setValue(RecipeMatcher.getAlmostThereRecipes(latestRecipes, pantryQuantities, allQuantities));
        matchingRecipes.setValue(RecipeMatcher.getMatchingRecipes(latestRecipes, pantryQuantities));
    }

    public LiveData<List<RecipeWithIngredients>> getMatchingRecipes() {
        return matchingRecipes;
    }

    /**
     * Only updates while {@link #getMatchingRecipes()} is observed, since that's the
     * mediator driving {@link #recompute()}.
     */
    public LiveData<List<AlmostThereRecipe>> getAlmostThereRecipes() {
        return almostThereRecipes;
    }

    /** How many ingredients the suggestions were worked out from, for the screen subtitle. */
    public LiveData<Integer> getPantryItemCount() {
        return pantryItemCount;
    }
}

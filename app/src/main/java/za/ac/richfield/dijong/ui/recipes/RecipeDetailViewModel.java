package za.ac.richfield.dijong.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Map;

import za.ac.richfield.dijong.data.PantryRepository;
import za.ac.richfield.dijong.data.RecipeRepository;
import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.util.RecipeMatcher;

/**
 * Supplies one recipe with its ingredients, plus the live pantry totals the recipe screen
 * needs to tick off what you already have.
 */
public class RecipeDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<Long> recipeId = new MutableLiveData<>();
    private final LiveData<RecipeWithIngredients> recipe;
    private final LiveData<Map<String, Double>> pantryStock;

    public RecipeDetailViewModel(@NonNull Application application) {
        super(application);
        RecipeRepository repository = new RecipeRepository(application);
        recipe = Transformations.switchMap(recipeId, repository::getRecipeWithIngredients);
        pantryStock = Transformations.map(new PantryRepository(application).getAllItems(),
                RecipeMatcher::buildPantryQuantityMap);
    }

    public void setRecipeId(long id) {
        recipeId.setValue(id);
    }

    public LiveData<RecipeWithIngredients> getRecipe() {
        return recipe;
    }

    /** Current pantry totals, so each ingredient can be ticked or marked missing. */
    public LiveData<Map<String, Double>> getPantryStock() {
        return pantryStock;
    }
}

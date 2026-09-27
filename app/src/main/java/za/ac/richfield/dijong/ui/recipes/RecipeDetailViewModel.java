package za.ac.richfield.dijong.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import za.ac.richfield.dijong.data.PantryRepository;
import za.ac.richfield.dijong.data.RecipeRepository;
import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.data.entity.PantryItem;

/**
 * Supplies one recipe with its ingredients, plus the live pantry contents the recipe screen
 * needs to tick off what you already have (and spot what you have but has expired).
 */
public class RecipeDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<Long> recipeId = new MutableLiveData<>();
    private final LiveData<RecipeWithIngredients> recipe;
    private final LiveData<List<PantryItem>> pantryItems;

    public RecipeDetailViewModel(@NonNull Application application) {
        super(application);
        RecipeRepository repository = new RecipeRepository(application);
        recipe = Transformations.switchMap(recipeId, repository::getRecipeWithIngredients);
        pantryItems = new PantryRepository(application).getAllItems();
    }

    public void setRecipeId(long id) {
        recipeId.setValue(id);
    }

    public LiveData<RecipeWithIngredients> getRecipe() {
        return recipe;
    }

    /** Everything in the pantry, expired items included, so the screen can say which is which. */
    public LiveData<List<PantryItem>> getPantryItems() {
        return pantryItems;
    }
}

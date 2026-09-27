package za.ac.richfield.dijong.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import za.ac.richfield.dijong.data.RecipeRepository;
import za.ac.richfield.dijong.data.RecipeWithIngredients;

public class RecipeDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<Long> recipeId = new MutableLiveData<>();
    private final LiveData<RecipeWithIngredients> recipe;

    public RecipeDetailViewModel(@NonNull Application application) {
        super(application);
        RecipeRepository repository = new RecipeRepository(application);
        recipe = Transformations.switchMap(recipeId, repository::getRecipeWithIngredients);
    }

    public void setRecipeId(long id) {
        recipeId.setValue(id);
    }

    public LiveData<RecipeWithIngredients> getRecipe() {
        return recipe;
    }
}

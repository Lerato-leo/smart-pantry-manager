package za.ac.richfield.dijong.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.data.entity.Recipe;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;

@Dao
public interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name")
    LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients();

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    LiveData<RecipeWithIngredients> getRecipeWithIngredients(long id);

    @Query("SELECT COUNT(*) FROM recipes")
    int countRecipes();

    @Insert
    long insertRecipe(Recipe recipe);

    @Insert
    void insertIngredients(List<RecipeIngredient> ingredients);

    @Query("DELETE FROM recipe_ingredients")
    void deleteAllIngredients();

    @Query("DELETE FROM recipes")
    void deleteAllRecipes();
}

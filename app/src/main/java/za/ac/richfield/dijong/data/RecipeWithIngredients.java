package za.ac.richfield.dijong.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.dijong.data.entity.Recipe;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;

/**
 * A recipe together with all of its required ingredients, fetched by Room in a
 * single extra query (WHERE recipeId IN (...)) rather than one query per recipe.
 */
public class RecipeWithIngredients {

    @Embedded
    public Recipe recipe;

    @Relation(parentColumn = "id", entityColumn = "recipeId")
    public List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe getRecipe() {
        return recipe;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public long getId() {
        return recipe.getId();
    }

    public String getName() {
        return recipe.getName();
    }

    public String getPrepSteps() {
        return recipe.getPrepSteps();
    }
}

package za.ac.richfield.smartpantry.model;

import java.util.List;

/**
 * Represents a recipe.
 */
public class Recipe {
    private long id;
    private String name;
    private String prepSteps;
    private List<RecipeIngredient> ingredients;

    public Recipe() {
    }

    public Recipe(long id, String name, String prepSteps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.prepSteps = prepSteps;
        this.ingredients = ingredients;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrepSteps() {
        return prepSteps;
    }

    public void setPrepSteps(String prepSteps) {
        this.prepSteps = prepSteps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prepSteps='" + prepSteps + '\'' +
                ", ingredients=" + ingredients +
                '}';
    }
}
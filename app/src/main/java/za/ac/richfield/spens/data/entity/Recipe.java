package za.ac.richfield.spens.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Represents a recipe. Ingredients live in a separate table; see {@link RecipeIngredient}
 * and {@link za.ac.richfield.spens.data.RecipeWithIngredients}.
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    @ColumnInfo(name = "prep_steps")
    private String prepSteps;

    public Recipe() {
        this.name = "";
    }

    @Ignore
    public Recipe(long id, @NonNull String name, String prepSteps) {
        this.id = id;
        this.name = name;
        this.prepSteps = prepSteps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getPrepSteps() {
        return prepSteps;
    }

    public void setPrepSteps(String prepSteps) {
        this.prepSteps = prepSteps;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prepSteps='" + prepSteps + '\'' +
                '}';
    }
}

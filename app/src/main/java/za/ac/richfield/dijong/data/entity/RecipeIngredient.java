package za.ac.richfield.dijong.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a single required ingredient for a {@link Recipe}. Rows are deleted
 * automatically when their parent recipe is deleted (ON DELETE CASCADE).
 */
@Entity(
        tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipeId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("recipeId")
)
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long recipeId;

    @NonNull
    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    @ColumnInfo(name = "required_quantity")
    private double requiredQuantity;

    private String unit;

    public RecipeIngredient() {
        this.ingredientName = "";
    }

    @Ignore
    public RecipeIngredient(long id, long recipeId, @NonNull String ingredientName, double requiredQuantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    @NonNull
    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(@NonNull String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "RecipeIngredient{" +
                "id=" + id +
                ", recipeId=" + recipeId +
                ", ingredientName='" + ingredientName + '\'' +
                ", requiredQuantity=" + requiredQuantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}

package za.ac.richfield.dijong.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import za.ac.richfield.dijong.data.IngredientCategory;

/**
 * One entry in the pantry: an ingredient, how much of it there is, which shelf it belongs
 * on, and (optionally) when it goes off. The expiry date is stored as an epoch day rather
 * than a formatted string so two dates can be compared with a plain {@code long} comparison
 * instead of parsing; see {@link za.ac.richfield.dijong.util.ExpiryDateConverter} for the
 * round trip to the date the UI shows.
 */
@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    private double quantity;

    private String unit;

    @Nullable
    @ColumnInfo(name = "expiry_date")
    private Long expiryDate;

    /** An {@link IngredientCategory} name, e.g. "DAIRY". Added in database version 2. */
    @NonNull
    @ColumnInfo(defaultValue = IngredientCategory.DEFAULT_KEY)
    private String category;

    public PantryItem() {
        this.name = "";
        this.category = IngredientCategory.DEFAULT_KEY;
    }

    @Ignore
    public PantryItem(long id, @NonNull String name, double quantity, String unit, @Nullable Long expiryDate) {
        this(id, name, quantity, unit, expiryDate, IngredientCategory.OTHER);
    }

    @Ignore
    public PantryItem(long id, @NonNull String name, double quantity, String unit, @Nullable Long expiryDate,
                      @NonNull IngredientCategory category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.category = category.name();
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Nullable
    public Long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(@Nullable Long expiryDate) {
        this.expiryDate = expiryDate;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    /** Room's setter; the UI goes through {@link #setCategory(IngredientCategory)}. */
    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    public void setCategory(@NonNull IngredientCategory category) {
        this.category = category.name();
    }

    @NonNull
    public IngredientCategory getCategoryEnum() {
        return IngredientCategory.fromKey(category);
    }

    @Override
    public String toString() {
        return "PantryItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", expiryDate=" + expiryDate +
                ", category='" + category + '\'' +
                '}';
    }
}

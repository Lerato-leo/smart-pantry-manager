package za.ac.richfield.spens.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * One entry in the spens: an ingredient, how much of it there is, and (optionally) when it
 * goes off. The expiry date is stored as an epoch day rather than a formatted string so two
 * dates can be compared with a plain {@code long} comparison instead of parsing; see
 * {@link za.ac.richfield.spens.util.ExpiryDateConverter} for the yyyy-MM-dd round trip the
 * UI actually shows.
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

    public PantryItem() {
        this.name = "";
    }

    @Ignore
    public PantryItem(long id, @NonNull String name, double quantity, String unit, @Nullable Long expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
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

    @Override
    public String toString() {
        return "PantryItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}

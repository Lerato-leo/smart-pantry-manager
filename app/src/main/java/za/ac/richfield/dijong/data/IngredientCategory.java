package za.ac.richfield.dijong.data;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import za.ac.richfield.dijong.R;

/**
 * The shelf an ingredient belongs on. Each category carries its own label, icon and colour
 * pair (a soft background and a strong foreground), used for the icon circle and chip on a
 * pantry card and the coloured dot in the add/edit form.
 *
 * <p>Stored in the database by {@link #name()}, so renaming a constant would orphan rows;
 * add new constants instead.
 */
public enum IngredientCategory {
    VEGETABLES(R.string.category_vegetables, R.drawable.ic_category_vegetables,
            R.color.category_vegetables_bg, R.color.category_vegetables_fg),
    FRUIT(R.string.category_fruit, R.drawable.ic_category_fruit,
            R.color.category_fruit_bg, R.color.category_fruit_fg),
    MEAT(R.string.category_meat, R.drawable.ic_category_meat,
            R.color.category_meat_bg, R.color.category_meat_fg),
    DAIRY(R.string.category_dairy, R.drawable.ic_category_dairy,
            R.color.category_dairy_bg, R.color.category_dairy_fg),
    GRAINS(R.string.category_grains, R.drawable.ic_category_grains,
            R.color.category_grains_bg, R.color.category_grains_fg),
    SPICES(R.string.category_spices, R.drawable.ic_category_spices,
            R.color.category_spices_bg, R.color.category_spices_fg),
    CANNED(R.string.category_canned, R.drawable.ic_category_canned,
            R.color.category_canned_bg, R.color.category_canned_fg),
    OTHER(R.string.category_other, R.drawable.ic_category_other,
            R.color.category_other_bg, R.color.category_other_fg);

    /** What the database column defaults to, for rows saved before categories existed. */
    public static final String DEFAULT_KEY = "OTHER";

    @StringRes
    public final int labelRes;
    @DrawableRes
    public final int iconRes;
    @ColorRes
    public final int backgroundColorRes;
    @ColorRes
    public final int foregroundColorRes;

    IngredientCategory(int labelRes, int iconRes, int backgroundColorRes, int foregroundColorRes) {
        this.labelRes = labelRes;
        this.iconRes = iconRes;
        this.backgroundColorRes = backgroundColorRes;
        this.foregroundColorRes = foregroundColorRes;
    }

    /** Reads a stored key back, falling back to {@link #OTHER} for anything unrecognised. */
    @NonNull
    public static IngredientCategory fromKey(@Nullable String key) {
        if (key != null) {
            for (IngredientCategory category : values()) {
                if (category.name().equals(key)) {
                    return category;
                }
            }
        }
        return OTHER;
    }
}

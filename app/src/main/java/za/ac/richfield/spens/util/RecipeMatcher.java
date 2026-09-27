package za.ac.richfield.spens.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.ac.richfield.spens.data.RecipeWithIngredients;
import za.ac.richfield.spens.data.entity.PantryItem;
import za.ac.richfield.spens.data.entity.RecipeIngredient;

/**
 * Decides which recipes a spens can currently produce, by comparing what a recipe calls for
 * against what's on hand. The rule is deliberately strict: every ingredient must be present in
 * at least the required quantity and a compatible unit, or the whole recipe is ruled out. There's
 * no partial-match "you're close!" mode and no unit conversion across measurement systems
 * (grams never satisfy a millilitre requirement, even for the same ingredient).
 *
 * <p>This class touches nothing Android-specific, so it can be exercised with plain JUnit.
 */
public final class RecipeMatcher {

    /** Joins a normalized ingredient name and unit into one lookup key for the stock map. */
    private static final String KEY_JOINER = "#";

    private RecipeMatcher() {
        // Static helpers only.
    }

    /**
     * Reduces an ingredient name to a comparable form: lowercase, trimmed, and singular where
     * a simple English plural is detected. "Tomatoes" and "tomato" must collapse to the same
     * key so a pantry entry written either way still matches a recipe.
     */
    public static String normalizeIngredientName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String name = rawName.trim().toLowerCase();
        if (name.endsWith("ies") && name.length() > 4) {
            // berries -> berry (but "pies" falls through to the plain -s rule below)
            return name.substring(0, name.length() - 3) + "y";
        }
        if (name.endsWith("es") && takesEsPlural(name.substring(0, name.length() - 2))) {
            // tomatoes -> tomato, peaches -> peach
            return name.substring(0, name.length() - 2);
        }
        if (name.endsWith("s") && !name.endsWith("ss") && !name.endsWith("us") && !name.endsWith("is")) {
            // onions -> onion, apples -> apple; glass, hummus and couscous stay as they are
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * English only adds "-es" (rather than "-s") after these endings, so only then is the
     * whole "es" part of the plural. Everywhere else the "e" belongs to the word itself:
     * "apples" is "apple" + "s", not "appl" + "es".
     */
    private static boolean takesEsPlural(String stem) {
        return stem.endsWith("o") || stem.endsWith("x") || stem.endsWith("z")
                || stem.endsWith("ch") || stem.endsWith("sh") || stem.endsWith("ss");
    }

    /**
     * Collapses the many ways a cook might spell a unit ("g", "gram", "Grams") down to one
     * canonical spelling, so quantities recorded under any of those spellings still add up.
     * Anything not in this kitchen's usual vocabulary is passed through unchanged rather than
     * rejected, since a home cook's own shorthand shouldn't break matching.
     */
    public static String normalizeUnit(String rawUnit) {
        if (rawUnit == null || rawUnit.isEmpty()) {
            return "";
        }
        String candidate = rawUnit.trim().toLowerCase();
        switch (candidate) {
            case "g":
            case "gram":
            case "grams":
                return "gram";
            case "kg":
            case "kilogram":
            case "kilograms":
                return "kilogram";
            case "mg":
            case "milligram":
            case "milligrams":
                return "milligram";
            case "ml":
            case "milliliter":
            case "milliliters":
                return "ml";
            case "l":
            case "liter":
            case "liters":
                return "liter";
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return "teaspoon";
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                return "tablespoon";
            case "cup":
            case "cups":
                return "cup";
            case "oz":
            case "ounce":
            case "ounces":
                return "ounce";
            case "lb":
            case "pound":
            case "pounds":
                return "pound";
            case "pcs":
            case "piece":
            case "pieces":
            case "unit":
            case "units":
                return "unit";
            case "slice":
            case "slices":
                return "slice";
            case "leaf":
            case "leaves":
                return "leaf";
            case "clove":
            case "cloves":
                return "clove";
            case "can":
            case "cans":
                return "can";
            case "bottle":
            case "bottles":
                return "bottle";
            case "sprig":
            case "sprigs":
                return "sprig";
            case "dash":
            case "dashes":
                return "dash";
            case "pinch":
            case "pinches":
                return "pinch";
            default:
                return candidate;
        }
    }

    /** Builds the lookup key shared by pantry stock and recipe requirements alike. */
    private static String stockKey(String ingredientName, String unit) {
        return normalizeIngredientName(ingredientName) + KEY_JOINER + normalizeUnit(unit);
    }

    /**
     * Totals up everything on the shelf into one map, so a recipe check is a handful of lookups
     * rather than scanning the whole pantry per ingredient. Two entries for the same ingredient
     * and unit (e.g. two cartons of milk logged separately) are summed rather than overwriting
     * one another.
     */
    public static Map<String, Double> buildPantryQuantityMap(List<PantryItem> stock) {
        Map<String, Double> onHandByKey = new HashMap<>();
        if (stock == null) {
            return onHandByKey;
        }
        for (PantryItem stockedItem : stock) {
            String key = stockKey(stockedItem.getName(), stockedItem.getUnit());
            onHandByKey.merge(key, stockedItem.getQuantity(), Double::sum);
        }
        return onHandByKey;
    }

    /**
     * @param requiredIngredients what the recipe calls for
     * @param onHandByKey         the spens's current stock, as built by {@link #buildPantryQuantityMap}
     * @return true only if every single ingredient clears its required quantity
     */
    public static boolean canMakeRecipe(List<RecipeIngredient> requiredIngredients, Map<String, Double> onHandByKey) {
        if (requiredIngredients == null || requiredIngredients.isEmpty()) {
            return false;
        }
        for (RecipeIngredient needed : requiredIngredients) {
            String key = stockKey(needed.getIngredientName(), needed.getUnit());
            Double available = onHandByKey.get(key);
            // A unit mismatch (e.g. the recipe wants grams but the pantry has millilitres of
            // the same ingredient) is treated as "don't have it" rather than guessing a
            // conversion, since guessing wrong would suggest an uncookable recipe as ready.
            if (available == null || available < needed.getRequiredQuantity()) {
                return false;
            }
        }
        return true;
    }

    /** Filters a recipe catalogue down to the ones the current stock can actually produce. */
    public static List<RecipeWithIngredients> getMatchingRecipes(List<RecipeWithIngredients> catalogue, Map<String, Double> onHandByKey) {
        List<RecipeWithIngredients> cookableNow = new ArrayList<>();
        if (catalogue == null) {
            return cookableNow;
        }
        for (RecipeWithIngredients candidate : catalogue) {
            if (canMakeRecipe(candidate.getIngredients(), onHandByKey)) {
                cookableNow.add(candidate);
            }
        }
        return cookableNow;
    }
}

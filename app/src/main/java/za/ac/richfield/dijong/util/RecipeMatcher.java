package za.ac.richfield.dijong.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.data.entity.PantryItem;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;

/**
 * Decides which recipes the current pantry stock can produce, by comparing what a recipe calls
 * for against what's on hand. The rule is deliberately strict: every ingredient must be present
 * in at least the required quantity, or the whole recipe is ruled out.
 *
 * <p>Quantities are compared in a shared base unit, so 1 kg of flour covers a recipe asking for
 * 500 g, and a 750 ml bottle of oil covers 2 tbsp. Conversion never crosses between mass and
 * volume, though: grams never satisfy a millilitre requirement, since that would mean guessing
 * the ingredient's density.
 *
 * <p>This class touches nothing Android-specific, so it can be exercised with plain JUnit.
 */
public final class RecipeMatcher {

    /** Joins a normalized ingredient name and unit into one lookup key for the stock map. */
    private static final String KEY_JOINER = "#";

    /** Absorbs floating-point noise from conversions, e.g. 0.3 kg becoming 300.00000000000006 g. */
    private static final double QUANTITY_TOLERANCE = 1e-6;

    private static final String BASE_MASS_UNIT = "gram";
    private static final String BASE_VOLUME_UNIT = "ml";

    /** How many grams one of each canonical mass unit is. */
    private static final Map<String, Double> GRAMS_PER_UNIT = new HashMap<>();

    /** How many millilitres one of each canonical volume unit is (metric kitchen measures). */
    private static final Map<String, Double> MILLILITRES_PER_UNIT = new HashMap<>();

    static {
        GRAMS_PER_UNIT.put("gram", 1.0);
        GRAMS_PER_UNIT.put("kilogram", 1000.0);
        GRAMS_PER_UNIT.put("milligram", 0.001);
        GRAMS_PER_UNIT.put("ounce", 28.35);
        GRAMS_PER_UNIT.put("pound", 453.6);

        MILLILITRES_PER_UNIT.put("ml", 1.0);
        MILLILITRES_PER_UNIT.put("liter", 1000.0);
        MILLILITRES_PER_UNIT.put("teaspoon", 5.0);
        MILLILITRES_PER_UNIT.put("tablespoon", 15.0);
        MILLILITRES_PER_UNIT.put("cup", 250.0);
    }

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

    /**
     * The unit a quantity is converted into before comparing: grams for anything measured by
     * mass, millilitres for anything measured by volume, and the unit itself for counts such
     * as "unit", "slice" or "can", which can't be converted.
     */
    public static String baseUnit(String rawUnit) {
        String unit = normalizeUnit(rawUnit);
        if (GRAMS_PER_UNIT.containsKey(unit)) {
            return BASE_MASS_UNIT;
        }
        if (MILLILITRES_PER_UNIT.containsKey(unit)) {
            return BASE_VOLUME_UNIT;
        }
        return unit;
    }

    /** Converts a quantity into {@link #baseUnit}, e.g. 1.5 kg -> 1500 (grams). */
    public static double toBaseQuantity(double quantity, String rawUnit) {
        String unit = normalizeUnit(rawUnit);
        if (GRAMS_PER_UNIT.containsKey(unit)) {
            return quantity * GRAMS_PER_UNIT.get(unit);
        }
        if (MILLILITRES_PER_UNIT.containsKey(unit)) {
            return quantity * MILLILITRES_PER_UNIT.get(unit);
        }
        return quantity;
    }

    /** Builds the lookup key shared by pantry stock and recipe requirements alike. */
    private static String stockKey(String ingredientName, String unit) {
        return normalizeIngredientName(ingredientName) + KEY_JOINER + baseUnit(unit);
    }

    /**
     * Totals up everything on the shelf into one map, so a recipe check is a handful of lookups
     * rather than scanning the whole pantry per ingredient. Two entries for the same ingredient
     * and unit (e.g. two cartons of milk logged separately) are summed rather than overwriting
     * one another, and so are entries in different units of the same kind (500 g + 1 kg).
     */
    public static Map<String, Double> buildPantryQuantityMap(List<PantryItem> stock) {
        Map<String, Double> onHandByKey = new HashMap<>();
        if (stock == null) {
            return onHandByKey;
        }
        for (PantryItem stockedItem : stock) {
            String key = stockKey(stockedItem.getName(), stockedItem.getUnit());
            double baseQuantity = toBaseQuantity(stockedItem.getQuantity(), stockedItem.getUnit());
            onHandByKey.merge(key, baseQuantity, Double::sum);
        }
        return onHandByKey;
    }

    /**
     * @param requiredIngredients what the recipe calls for
     * @param onHandByKey         the pantry's current stock, as built by {@link #buildPantryQuantityMap}
     * @return true only if every single ingredient clears its required quantity
     */
    public static boolean canMakeRecipe(List<RecipeIngredient> requiredIngredients, Map<String, Double> onHandByKey) {
        if (requiredIngredients == null || requiredIngredients.isEmpty()) {
            return false;
        }
        for (RecipeIngredient needed : requiredIngredients) {
            if (missingQuantity(needed, onHandByKey) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * How much more of one ingredient the pantry needs, in the recipe's own unit: 0 when there
     * is enough, the full required amount when there is none at all.
     */
    private static double missingQuantity(RecipeIngredient needed, Map<String, Double> onHandByKey) {
        String key = stockKey(needed.getIngredientName(), needed.getUnit());
        Double available = onHandByKey.get(key);
        // A mass/volume mismatch (the recipe wants grams, the pantry has millilitres of the
        // same ingredient) lands on a different key, so it reads as "don't have it" rather
        // than guessing a conversion that could suggest an uncookable recipe as ready.
        if (available == null) {
            return needed.getRequiredQuantity();
        }
        double required = toBaseQuantity(needed.getRequiredQuantity(), needed.getUnit());
        if (available + QUANTITY_TOLERANCE >= required) {
            return 0;
        }
        // Scale the base-unit gap back into the recipe's unit, so "need 200 g more" is
        // reported in grams even if the pantry stocks the ingredient in kilograms.
        double baseUnitsPerRecipeUnit = required / needed.getRequiredQuantity();
        return (required - available) / baseUnitsPerRecipeUnit;
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

    /**
     * Recipes that are exactly one ingredient short, for the separate "Almost there" list.
     * These are never suggestions: the strict list above stays the only thing that claims a
     * recipe is cookable. A recipe with enough of everything except one ingredient, or with
     * one ingredient missing entirely, qualifies; two or more shortfalls do not.
     */
    public static List<AlmostThereRecipe> getAlmostThereRecipes(List<RecipeWithIngredients> catalogue, Map<String, Double> onHandByKey) {
        List<AlmostThereRecipe> oneShort = new ArrayList<>();
        if (catalogue == null) {
            return oneShort;
        }
        for (RecipeWithIngredients candidate : catalogue) {
            List<RecipeIngredient> ingredients = candidate.getIngredients();
            if (ingredients == null || ingredients.isEmpty()) {
                continue;
            }
            RecipeIngredient onlyShortfall = null;
            double onlyMissingQuantity = 0;
            int shortfalls = 0;
            for (RecipeIngredient needed : ingredients) {
                double missing = missingQuantity(needed, onHandByKey);
                if (missing > 0) {
                    shortfalls++;
                    onlyShortfall = needed;
                    onlyMissingQuantity = missing;
                    if (shortfalls > 1) {
                        break;
                    }
                }
            }
            if (shortfalls == 1) {
                oneShort.add(new AlmostThereRecipe(candidate, onlyShortfall, onlyMissingQuantity));
            }
        }
        return oneShort;
    }

    /** A recipe one ingredient short of cookable, and what it's short of. */
    public static final class AlmostThereRecipe {

        private final RecipeWithIngredients recipe;
        private final RecipeIngredient missingIngredient;
        private final double missingQuantity;

        public AlmostThereRecipe(RecipeWithIngredients recipe, RecipeIngredient missingIngredient, double missingQuantity) {
            this.recipe = recipe;
            this.missingIngredient = missingIngredient;
            this.missingQuantity = missingQuantity;
        }

        public RecipeWithIngredients getRecipe() {
            return recipe;
        }

        public RecipeIngredient getMissingIngredient() {
            return missingIngredient;
        }

        /** How much more is needed, in the missing ingredient's recipe unit. */
        public double getMissingQuantity() {
            return missingQuantity;
        }

        /** True when there's none of the ingredient at all, rather than just not enough. */
        public boolean isMissingEntirely() {
            return Math.abs(missingQuantity - missingIngredient.getRequiredQuantity()) < QUANTITY_TOLERANCE;
        }
    }
}

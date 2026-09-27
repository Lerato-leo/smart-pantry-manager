package za.ac.richfield.dijong.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.data.entity.PantryItem;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;

/**
 * Decides which recipes the current pantry stock can produce, by comparing what a recipe calls
 * for against what's on hand. The rule is deliberately strict: every ingredient must be present
 * in at least the required quantity, or the whole recipe is ruled out. Strict also means only
 * usable stock counts: food past its expiry date is left out (see
 * {@link #buildPantryQuantityMap(List, long)}), and an ingredient listed twice in a recipe must
 * be covered twice over.
 *
 * <p>Names are compared loosely enough for real-world typing: case, plurals, punctuation and
 * spacing don't matter, and a few South African alternative names are treated as the same
 * ingredient (see {@link #NAME_ALIASES}). Tap water is never treated as an ingredient.
 *
 * <p>Quantities are compared in a shared base unit, so 1 kg of flour covers a recipe asking for
 * 500 g, and a 750 ml bottle of oil covers 2 tbsp. Mass and volume are only converted into each
 * other for the handful of ingredients in {@link #GRAMS_PER_MILLILITRE}: things recipes measure
 * in spoons but shops sell by weight, like curry powder or chutney. For anything else, grams
 * never satisfy a millilitre requirement, since that would mean guessing the density.
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
        MILLILITRES_PER_UNIT.put("fluid_ounce", 29.57);
    }

    /**
     * Approximate weight of one millilitre of ingredients that recipes measure with spoons
     * but people buy and stock by weight: a 100 g packet of curry powder should cover a recipe
     * asking for 1 tbsp (about 6 g). Values are typical kitchen densities; being a few percent
     * out only matters when the pantry holds almost exactly the amount a recipe needs.
     */
    private static final Map<String, Double> GRAMS_PER_MILLILITRE = new HashMap<>();

    static {
        GRAMS_PER_MILLILITRE.put("curry powder", 0.42);
        GRAMS_PER_MILLILITRE.put("cinnamon", 0.52);
        GRAMS_PER_MILLILITRE.put("ginger", 0.36);
        GRAMS_PER_MILLILITRE.put("sugar", 0.85);
        GRAMS_PER_MILLILITRE.put("flour", 0.53);
        GRAMS_PER_MILLILITRE.put("yeast", 0.6);
        GRAMS_PER_MILLILITRE.put("butter", 0.96);
        GRAMS_PER_MILLILITRE.put("honey", 1.42);
        GRAMS_PER_MILLILITRE.put("apricot jam", 1.33);
        GRAMS_PER_MILLILITRE.put("chutney", 1.2);
        GRAMS_PER_MILLILITRE.put("baking powder", 0.9);
        GRAMS_PER_MILLILITRE.put("bicarbonate of soda", 0.9);
    }

    /**
     * Different names shoppers use for the same thing, mapped to the name recipes use. Only
     * true equivalents belong here: "cake flour" is what South Africans call plain flour, but
     * "chicken stock" is not chicken, so there's no loose word matching.
     */
    private static final Map<String, String> NAME_ALIASES = new HashMap<>();

    static {
        NAME_ALIASES.put("mielie meal", "maize meal");
        NAME_ALIASES.put("mealie meal", "maize meal");
        NAME_ALIASES.put("mielie pap", "maize meal");
        NAME_ALIASES.put("cake flour", "flour");
        NAME_ALIASES.put("plain flour", "flour");
        NAME_ALIASES.put("all purpose flour", "flour");
        NAME_ALIASES.put("white sugar", "sugar");
        NAME_ALIASES.put("mince", "beef mince");
        NAME_ALIASES.put("minced beef", "beef mince");
        NAME_ALIASES.put("ground beef", "beef mince");
        NAME_ALIASES.put("cheddar", "cheddar cheese");
        NAME_ALIASES.put("butternut squash", "butternut");
        NAME_ALIASES.put("full cream milk", "milk");
        NAME_ALIASES.put("fresh cream", "cream");
        NAME_ALIASES.put("brown onion", "onion");
        NAME_ALIASES.put("white onion", "onion");
        NAME_ALIASES.put("free range egg", "egg");
        NAME_ALIASES.put("instant yeast", "yeast");
        NAME_ALIASES.put("dry yeast", "yeast");
    }

    /** Comes from the tap, so a recipe that mentions it never waits on the pantry for it. */
    private static final Set<String> ALWAYS_AVAILABLE = new HashSet<>(Arrays.asList("water", "tap water"));

    private RecipeMatcher() {
        // Static helpers only.
    }

    /**
     * Reduces an ingredient name to a comparable form: lowercase, with hyphens, punctuation and
     * repeated spaces tidied away, singular where a simple English plural is detected, and
     * mapped through {@link #NAME_ALIASES}. "Tomatoes", "tomato" and " Tomato " must collapse to
     * the same key, and so must "Mielie-meal" and "maize meal", so a pantry entry written any of
     * those ways still matches a recipe.
     */
    public static String normalizeIngredientName(String rawName) {
        if (rawName == null) {
            return "";
        }
        // Apostrophes are kept ("ouma's"); anything else that isn't a letter or digit becomes a space
        String name = rawName.toLowerCase().replaceAll("[^a-z0-9']+", " ").trim();
        String singular = singularise(name);
        String alias = NAME_ALIASES.get(singular);
        return alias != null ? alias : singular;
    }

    /** Singularises the last word, which is the one that carries the plural in English. */
    private static String singularise(String name) {
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

    /** True for things like tap water that a recipe can always assume are there. */
    public static boolean isAlwaysAvailable(String ingredientName) {
        return ALWAYS_AVAILABLE.contains(normalizeIngredientName(ingredientName));
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
            case "fl oz":
            case "floz":
            case "fluid ounce":
            case "fluid ounces":
                return "fluid_ounce";
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
        return buildPantryQuantityMap(stock, Long.MIN_VALUE);
    }

    /**
     * Like {@link #buildPantryQuantityMap(List)}, but leaves out anything that expired before
     * {@code todayEpochDay}: food past its date can't be cooked with, so it mustn't make a
     * recipe look ready. Something expiring today still counts.
     */
    public static Map<String, Double> buildPantryQuantityMap(List<PantryItem> stock, long todayEpochDay) {
        Map<String, Double> onHandByKey = new HashMap<>();
        if (stock == null) {
            return onHandByKey;
        }
        for (PantryItem stockedItem : stock) {
            Long expiry = stockedItem.getExpiryDate();
            if (expiry != null && expiry < todayEpochDay) {
                continue;
            }
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
        if (requiredIngredients == null) {
            return false;
        }
        List<RecipeIngredient> needs = requirements(requiredIngredients);
        if (needs.isEmpty()) {
            // Nothing (or only tap water) to take from the pantry: not a pantry recipe
            return false;
        }
        for (RecipeIngredient needed : needs) {
            if (missingQuantity(needed, onHandByKey) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * What a recipe actually needs from the pantry: tap water dropped, and lines for the same
     * ingredient in the same kind of measure added together (a recipe using 1 onion in the
     * sauce and 1 in the salad needs 2), expressed in the unit of the first such line.
     */
    static List<RecipeIngredient> requirements(List<RecipeIngredient> ingredients) {
        Map<String, RecipeIngredient> merged = new LinkedHashMap<>();
        for (RecipeIngredient line : ingredients) {
            if (isAlwaysAvailable(line.getIngredientName())) {
                continue;
            }
            String key = stockKey(line.getIngredientName(), line.getUnit());
            RecipeIngredient earlier = merged.get(key);
            if (earlier == null) {
                merged.put(key, line);
                continue;
            }
            double earlierUnitSize = toBaseQuantity(1, earlier.getUnit());
            double combined = earlier.getRequiredQuantity()
                    + toBaseQuantity(line.getRequiredQuantity(), line.getUnit()) / earlierUnitSize;
            merged.put(key, new RecipeIngredient(earlier.getId(), earlier.getRecipeId(),
                    earlier.getIngredientName(), combined, earlier.getUnit()));
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * True when the pantry holds some of this ingredient, even if not enough, so the recipe
     * screen can say "Not enough" instead of "Missing".
     */
    public static boolean hasSome(RecipeIngredient needed, Map<String, Double> onHandByKey) {
        Double available = availableInRecipeBase(needed, onHandByKey);
        return available != null && available > QUANTITY_TOLERANCE;
    }

    /** True when the pantry holds at least the required amount of this one ingredient. */
    public static boolean isInStock(RecipeIngredient needed, Map<String, Double> onHandByKey) {
        return isAlwaysAvailable(needed.getIngredientName()) || missingQuantity(needed, onHandByKey) <= 0;
    }

    /**
     * How much more of one ingredient the pantry needs, in the recipe's own unit: 0 when there
     * is enough, the full required amount when there is none at all.
     */
    private static double missingQuantity(RecipeIngredient needed, Map<String, Double> onHandByKey) {
        Double available = availableInRecipeBase(needed, onHandByKey);
        // A mass/volume mismatch (the recipe wants grams, the pantry has millilitres of the
        // same ingredient) lands on a different key, so unless the ingredient has a known
        // density it reads as "don't have it" rather than guessing a conversion that could
        // suggest an uncookable recipe as ready.
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

    /**
     * How much of an ingredient the pantry holds, in the base unit the recipe measures it in
     * (grams or millilitres), or null if there's none it can be compared with. Stock kept by
     * weight counts toward a spoon measure, and the other way round, for ingredients listed
     * in {@link #GRAMS_PER_MILLILITRE}.
     */
    private static Double availableInRecipeBase(RecipeIngredient needed, Map<String, Double> onHandByKey) {
        String name = normalizeIngredientName(needed.getIngredientName());
        String recipeBase = baseUnit(needed.getUnit());
        Double sameUnit = onHandByKey.get(name + KEY_JOINER + recipeBase);

        Double density = GRAMS_PER_MILLILITRE.get(name);
        Double converted = null;
        if (density != null && BASE_VOLUME_UNIT.equals(recipeBase)) {
            Double grams = onHandByKey.get(name + KEY_JOINER + BASE_MASS_UNIT);
            converted = grams == null ? null : grams / density;
        } else if (density != null && BASE_MASS_UNIT.equals(recipeBase)) {
            Double millilitres = onHandByKey.get(name + KEY_JOINER + BASE_VOLUME_UNIT);
            converted = millilitres == null ? null : millilitres * density;
        }

        if (sameUnit == null) {
            return converted;
        }
        return converted == null ? sameUnit : sameUnit + converted;
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
        return getAlmostThereRecipes(catalogue, onHandByKey, onHandByKey);
    }

    /**
     * Like {@link #getAlmostThereRecipes(List, Map)}, judged on usable stock, but also notes
     * when the one shortfall is only there because the pantry's supply has expired, so the
     * card can say "Expired: milk" rather than "Missing: milk".
     *
     * @param usableStock stock that hasn't expired, from {@link #buildPantryQuantityMap(List, long)}
     * @param allStock    everything on the shelf, expired included
     */
    public static List<AlmostThereRecipe> getAlmostThereRecipes(List<RecipeWithIngredients> catalogue,
                                                                Map<String, Double> usableStock,
                                                                Map<String, Double> allStock) {
        Map<String, Double> onHandByKey = usableStock;
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
            for (RecipeIngredient needed : requirements(ingredients)) {
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
                boolean onlyExpired = missingQuantity(onlyShortfall, allStock) <= 0;
                oneShort.add(new AlmostThereRecipe(candidate, onlyShortfall, onlyMissingQuantity, onlyExpired));
            }
        }
        return oneShort;
    }

    /** A recipe one ingredient short of cookable, and what it's short of. */
    public static final class AlmostThereRecipe {

        private final RecipeWithIngredients recipe;
        private final RecipeIngredient missingIngredient;
        private final double missingQuantity;
        private final boolean onlyExpired;

        public AlmostThereRecipe(RecipeWithIngredients recipe, RecipeIngredient missingIngredient, double missingQuantity) {
            this(recipe, missingIngredient, missingQuantity, false);
        }

        public AlmostThereRecipe(RecipeWithIngredients recipe, RecipeIngredient missingIngredient,
                                 double missingQuantity, boolean onlyExpired) {
            this.recipe = recipe;
            this.missingIngredient = missingIngredient;
            this.missingQuantity = missingQuantity;
            this.onlyExpired = onlyExpired;
        }

        /** True when there's enough of the ingredient on the shelf, but it has expired. */
        public boolean isOnlyExpired() {
            return onlyExpired;
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

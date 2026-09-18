package za.ac.richfield.smartpantry.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * Utility class for matching recipes against pantry ingredients.
 * Implements a strict-matching rule: a recipe can be made only if every required ingredient
 * is present in the pantry in sufficient quantity, with normalized names and units.
 * This class has no Android dependencies and can be easily unit-tested.
 */
public class RecipeMatcher {

    private RecipeMatcher() {
        // Prevent instantiation
    }

    /**
     * Normalizes an ingredient name for comparison.
     * Converts to lowercase, trims whitespace, and removes common plural suffixes.
     *
     * @param name the ingredient name to normalize
     * @return the normalized name
     */
    public static String normalizeIngredientName(String name) {
        if (name == null) {
            return "";
        }
        String normalized = name.trim().toLowerCase();
        // Remove trailing 's' or 'es' for simple plurals (e.g., tomatoes -> tomato)
        // But be careful not to remove 's' from words that are singular and end with 's' (e.g., glasses).
        // We'll apply a simple rule: if the word ends with 'ies', replace with 'y'; else if ends with 'es', remove 'es'; else if ends with 's' and not 'ss', remove 's'.
        if (normalized.endsWith("ies")) {
            normalized = normalized.substring(0, normalized.length() - 3) + "y";
        } else if (normalized.endsWith("es")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("s") && !normalized.endsWith("ss")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * Normalizes a unit for comparison.
     * Converts to lowercase, trims whitespace, and maps common variations to a standard form.
     *
     * @param unit the unit to normalize (may be null or empty)
     * @return the normalized unit, or empty string if the input was null or empty
     */
    public static String normalizeUnit(String unit) {
        if (unit == null || unit.isEmpty()) {
            return "";
        }
        String normalized = unit.trim().toLowerCase();
        // Map common unit variations
        switch (normalized) {
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
                // If we don't recognize the unit, return it as-is (lowercase and trimmed)
                return normalized;
        }
    }

    /**
     * Checks if a recipe can be made with the given pantry ingredients.
     * The pantry is represented as a map where the key is "normalizedIngredientName#normalizedUnit"
     * and the value is the total quantity available for that ingredient-unit pair.
     *
     * @param recipe the recipe to check
     * @param pantry a map of normalized ingredient#unit to available quantity
     * @return true if the recipe can be made, false otherwise
     */
    public static boolean canMakeRecipe(Recipe recipe, Map<String, Double> pantry) {
        if (recipe == null || recipe.getIngredients() == null) {
            return false;
        }
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            String normName = normalizeIngredientName(ingredient.getIngredientName());
            String normUnit = normalizeUnit(ingredient.getUnit());
            String key = normName + "#" + normUnit;
            double required = ingredient.getRequiredQuantity();

            Double available = pantry.get(key);
            if (available == null || available < required) {
                // If the exact unit doesn't match, we could try to convert, but the requirement
                // says to normalize basic units and then compare. We'll treat mismatched units as not available.
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a list of recipes that can be made with the given pantry.
     *
     * @param recipes the list of all recipes
     * @param pantry a map of normalized ingredient#unit to available quantity (from pantry items)
     * @return a list of recipes that can be made
     */
    public static List<Recipe> getMatchingRecipes(List<Recipe> recipes, Map<String, Double> pantry) {
        List<Recipe> matches = new java.util.ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMakeRecipe(recipe, pantry)) {
                matches.add(recipe);
            }
        }
        return matches;
    }
}
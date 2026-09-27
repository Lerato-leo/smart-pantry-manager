package za.ac.richfield.smartpantry.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

public class RecipeMatcherTest {

    @Test
    public void normalizeIngredientName_lowercasesAndTrims() {
        assertEquals("tomato", RecipeMatcher.normalizeIngredientName("  Tomato  "));
    }

    @Test
    public void normalizeIngredientName_handlesIesPlural() {
        assertEquals("berry", RecipeMatcher.normalizeIngredientName("berries"));
    }

    @Test
    public void normalizeIngredientName_handlesEsPlural() {
        assertEquals("tomato", RecipeMatcher.normalizeIngredientName("tomatoes"));
    }

    @Test
    public void normalizeIngredientName_handlesSimplePlural() {
        assertEquals("onion", RecipeMatcher.normalizeIngredientName("onions"));
    }

    @Test
    public void normalizeIngredientName_keepsSingularDoubleSEndings() {
        assertEquals("glass", RecipeMatcher.normalizeIngredientName("glass"));
    }

    @Test
    public void normalizeIngredientName_nullReturnsEmpty() {
        assertEquals("", RecipeMatcher.normalizeIngredientName(null));
    }

    @Test
    public void normalizeUnit_mapsAbbreviationsToCanonicalForm() {
        assertEquals("gram", RecipeMatcher.normalizeUnit("g"));
        assertEquals("gram", RecipeMatcher.normalizeUnit("Grams"));
        assertEquals("tablespoon", RecipeMatcher.normalizeUnit("tbsp"));
        assertEquals("unit", RecipeMatcher.normalizeUnit("pcs"));
    }

    @Test
    public void normalizeUnit_unknownUnitReturnedAsIs() {
        assertEquals("bunch", RecipeMatcher.normalizeUnit(" Bunch "));
    }

    @Test
    public void normalizeUnit_nullOrEmptyReturnsEmpty() {
        assertEquals("", RecipeMatcher.normalizeUnit(null));
        assertEquals("", RecipeMatcher.normalizeUnit(""));
    }

    @Test
    public void canMakeRecipe_trueWhenAllIngredientsSufficient() {
        Recipe recipe = recipeWith(
                ingredient("Tomatoes", 2, "kg"),
                ingredient("Onion", 1, "unit"));

        Map<String, Double> pantry = new HashMap<>();
        pantry.put("tomato#kilogram", 3.0);
        pantry.put("onion#unit", 2.0);

        assertTrue(RecipeMatcher.canMakeRecipe(recipe, pantry));
    }

    @Test
    public void canMakeRecipe_falseWhenIngredientMissing() {
        Recipe recipe = recipeWith(ingredient("Tomatoes", 2, "kg"));
        Map<String, Double> pantry = new HashMap<>();

        assertFalse(RecipeMatcher.canMakeRecipe(recipe, pantry));
    }

    @Test
    public void canMakeRecipe_falseWhenQuantityInsufficient() {
        Recipe recipe = recipeWith(ingredient("Tomatoes", 2, "kg"));
        Map<String, Double> pantry = new HashMap<>();
        pantry.put("tomato#kilogram", 1.0);

        assertFalse(RecipeMatcher.canMakeRecipe(recipe, pantry));
    }

    @Test
    public void canMakeRecipe_falseWhenUnitMismatch() {
        Recipe recipe = recipeWith(ingredient("Tomatoes", 2, "kg"));
        Map<String, Double> pantry = new HashMap<>();
        pantry.put("tomato#gram", 5000.0);

        assertFalse(RecipeMatcher.canMakeRecipe(recipe, pantry));
    }

    @Test
    public void canMakeRecipe_falseForNullRecipe() {
        assertFalse(RecipeMatcher.canMakeRecipe(null, new HashMap<>()));
    }

    @Test
    public void getMatchingRecipes_returnsOnlyMakeableRecipes() {
        Recipe makeable = recipeWith(ingredient("Onion", 1, "unit"));
        Recipe notMakeable = recipeWith(ingredient("Beef", 1, "kg"));
        List<Recipe> recipes = Arrays.asList(makeable, notMakeable);

        Map<String, Double> pantry = new HashMap<>();
        pantry.put("onion#unit", 5.0);

        List<Recipe> matches = RecipeMatcher.getMatchingRecipes(recipes, pantry);

        assertEquals(1, matches.size());
        assertEquals(makeable, matches.get(0));
    }

    private static Recipe recipeWith(RecipeIngredient... ingredients) {
        return new Recipe(1L, "Test Recipe", "Test steps", Arrays.asList(ingredients));
    }

    private static RecipeIngredient ingredient(String name, double quantity, String unit) {
        return new RecipeIngredient(1L, 1L, name, quantity, unit);
    }
}

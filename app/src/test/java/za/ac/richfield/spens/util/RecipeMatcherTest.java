package za.ac.richfield.spens.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.ac.richfield.spens.data.RecipeWithIngredients;
import za.ac.richfield.spens.data.entity.PantryItem;
import za.ac.richfield.spens.data.entity.Recipe;
import za.ac.richfield.spens.data.entity.RecipeIngredient;

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
    public void normalizeIngredientName_handlesEsPluralAfterSibilants() {
        assertEquals("peach", RecipeMatcher.normalizeIngredientName("peaches"));
        assertEquals("box", RecipeMatcher.normalizeIngredientName("boxes"));
    }

    @Test
    public void normalizeIngredientName_singularAndPluralOfWordsEndingInEMatch() {
        // Regression: "apples" used to lose its "es" and become "appl", so it never
        // matched a recipe asking for "apple".
        assertEquals("apple", RecipeMatcher.normalizeIngredientName("apples"));
        assertEquals("apple", RecipeMatcher.normalizeIngredientName("apple"));
        assertEquals("clove", RecipeMatcher.normalizeIngredientName("cloves"));
        assertEquals("cheddar cheese", RecipeMatcher.normalizeIngredientName("Cheddar Cheeses"));
    }

    @Test
    public void normalizeIngredientName_shortIesWordsKeepTheirE() {
        assertEquals("pie", RecipeMatcher.normalizeIngredientName("pies"));
    }

    @Test
    public void normalizeIngredientName_keepsSingularDoubleSEndings() {
        assertEquals("glass", RecipeMatcher.normalizeIngredientName("glass"));
    }

    @Test
    public void normalizeIngredientName_keepsSingularUsEndings() {
        assertEquals("hummus", RecipeMatcher.normalizeIngredientName("hummus"));
        assertEquals("couscous", RecipeMatcher.normalizeIngredientName("couscous"));
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
    public void buildPantryQuantityMap_sumsQuantitiesForSameNormalizedKey() {
        List<PantryItem> items = Arrays.asList(
                pantryItem("Tomato", 1, "kg"),
                pantryItem("Tomatoes", 2, "kg"));

        Map<String, Double> map = RecipeMatcher.buildPantryQuantityMap(items);

        assertEquals(3.0, map.get("tomato#kilogram"), 0.0001);
    }

    @Test
    public void canMakeRecipe_trueWhenAllIngredientsSufficient() {
        List<RecipeIngredient> ingredients = Arrays.asList(
                ingredient("Tomatoes", 2, "kg"),
                ingredient("Onion", 1, "unit"));

        Map<String, Double> pantry = new HashMap<>();
        pantry.put("tomato#kilogram", 3.0);
        pantry.put("onion#unit", 2.0);

        assertTrue(RecipeMatcher.canMakeRecipe(ingredients, pantry));
    }

    @Test
    public void canMakeRecipe_falseWhenIngredientMissing() {
        List<RecipeIngredient> ingredients = Arrays.asList(ingredient("Tomatoes", 2, "kg"));
        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, new HashMap<>()));
    }

    @Test
    public void canMakeRecipe_falseWhenQuantityInsufficient() {
        List<RecipeIngredient> ingredients = Arrays.asList(ingredient("Tomatoes", 2, "kg"));
        Map<String, Double> pantry = new HashMap<>();
        pantry.put("tomato#kilogram", 1.0);

        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, pantry));
    }

    @Test
    public void canMakeRecipe_falseWhenUnitMismatch() {
        List<RecipeIngredient> ingredients = Arrays.asList(ingredient("Tomatoes", 2, "kg"));
        Map<String, Double> pantry = new HashMap<>();
        pantry.put("tomato#gram", 5000.0);

        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, pantry));
    }

    @Test
    public void canMakeRecipe_falseForEmptyIngredientList() {
        assertFalse(RecipeMatcher.canMakeRecipe(new ArrayList<>(), new HashMap<>()));
    }

    @Test
    public void getMatchingRecipes_returnsOnlyMakeableRecipes() {
        RecipeWithIngredients makeable = recipeWith(1L, "Makeable", ingredient("Onion", 1, "unit"));
        RecipeWithIngredients notMakeable = recipeWith(2L, "Not makeable", ingredient("Beef", 1, "kg"));
        List<RecipeWithIngredients> recipes = Arrays.asList(makeable, notMakeable);

        Map<String, Double> pantry = new HashMap<>();
        pantry.put("onion#unit", 5.0);

        List<RecipeWithIngredients> matches = RecipeMatcher.getMatchingRecipes(recipes, pantry);

        assertEquals(1, matches.size());
        assertEquals(makeable, matches.get(0));
    }

    private static PantryItem pantryItem(String name, double quantity, String unit) {
        return new PantryItem(1L, name, quantity, unit, null);
    }

    private static RecipeWithIngredients recipeWith(long id, String name, RecipeIngredient... ingredients) {
        RecipeWithIngredients recipe = new RecipeWithIngredients();
        recipe.recipe = new Recipe(id, name, "Test steps");
        recipe.ingredients = Arrays.asList(ingredients);
        return recipe;
    }

    private static RecipeIngredient ingredient(String name, double quantity, String unit) {
        return new RecipeIngredient(1L, 1L, name, quantity, unit);
    }
}

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
    public void baseUnit_groupsMassAndVolumeUnits() {
        assertEquals("gram", RecipeMatcher.baseUnit("kg"));
        assertEquals("gram", RecipeMatcher.baseUnit("oz"));
        assertEquals("ml", RecipeMatcher.baseUnit("l"));
        assertEquals("ml", RecipeMatcher.baseUnit("tbsp"));
        assertEquals("unit", RecipeMatcher.baseUnit("pcs"));
    }

    @Test
    public void toBaseQuantity_convertsIntoBaseUnit() {
        assertEquals(1500.0, RecipeMatcher.toBaseQuantity(1.5, "kg"), 0.0001);
        assertEquals(30.0, RecipeMatcher.toBaseQuantity(2, "tbsp"), 0.0001);
        assertEquals(3.0, RecipeMatcher.toBaseQuantity(3, "slices"), 0.0001);
    }

    @Test
    public void buildPantryQuantityMap_sumsQuantitiesForSameNormalizedKey() {
        List<PantryItem> items = Arrays.asList(
                pantryItem("Tomato", 1, "kg"),
                pantryItem("Tomatoes", 500, "g"));

        Map<String, Double> map = RecipeMatcher.buildPantryQuantityMap(items);

        assertEquals(1500.0, map.get("tomato#gram"), 0.0001);
    }

    @Test
    public void canMakeRecipe_trueWhenAllIngredientsSufficient() {
        List<RecipeIngredient> ingredients = Arrays.asList(
                ingredient("Tomatoes", 2, "kg"),
                ingredient("Onion", 1, "unit"));

        Map<String, Double> pantry = pantry(
                pantryItem("tomato", 3, "kg"),
                pantryItem("onions", 2, "unit"));

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
        Map<String, Double> pantry = pantry(pantryItem("tomato", 1, "kg"));

        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, pantry));
    }

    @Test
    public void canMakeRecipe_falseWhenOneOfFiveIngredientsMissing() {
        // The brief's own example: 4 of 5 ingredients is still not a match.
        List<RecipeIngredient> ingredients = Arrays.asList(
                ingredient("Flour", 500, "g"),
                ingredient("Egg", 2, "unit"),
                ingredient("Milk", 250, "ml"),
                ingredient("Butter", 50, "g"),
                ingredient("Sugar", 100, "g"));
        Map<String, Double> pantry = pantry(
                pantryItem("flour", 1, "kg"),
                pantryItem("eggs", 6, "unit"),
                pantryItem("milk", 1, "l"),
                pantryItem("butter", 250, "g"));

        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, pantry));
    }

    @Test
    public void canMakeRecipe_convertsBetweenMassUnits() {
        List<RecipeIngredient> ingredients = Arrays.asList(ingredient("Flour", 500, "g"));

        assertTrue(RecipeMatcher.canMakeRecipe(ingredients, pantry(pantryItem("flour", 1, "kg"))));
        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, pantry(pantryItem("flour", 0.4, "kg"))));
    }

    @Test
    public void canMakeRecipe_convertsBetweenVolumeUnits() {
        List<RecipeIngredient> ingredients = Arrays.asList(ingredient("Vegetable oil", 2, "tbsp"));

        assertTrue(RecipeMatcher.canMakeRecipe(ingredients, pantry(pantryItem("vegetable oil", 750, "ml"))));
        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, pantry(pantryItem("vegetable oil", 1, "tsp"))));
    }

    @Test
    public void canMakeRecipe_exactQuantityAfterConversionIsEnough() {
        // 0.3 kg -> 300.00000000000006 g in floating point; 300 g must still count as enough.
        List<RecipeIngredient> ingredients = Arrays.asList(ingredient("Beef", 0.3, "kg"));

        assertTrue(RecipeMatcher.canMakeRecipe(ingredients, pantry(pantryItem("beef", 300, "g"))));
    }

    @Test
    public void canMakeRecipe_falseWhenMassAndVolumeMismatch() {
        List<RecipeIngredient> ingredients = Arrays.asList(ingredient("Cream", 250, "ml"));

        assertFalse(RecipeMatcher.canMakeRecipe(ingredients, pantry(pantryItem("cream", 5, "kg"))));
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

        List<RecipeWithIngredients> matches =
                RecipeMatcher.getMatchingRecipes(recipes, pantry(pantryItem("onion", 5, "unit")));

        assertEquals(1, matches.size());
        assertEquals(makeable, matches.get(0));
    }

    @Test
    public void getAlmostThereRecipes_includesRecipeWithOneIngredientMissing() {
        RecipeWithIngredients pap = recipeWith(1L, "Pap",
                ingredient("Maize meal", 500, "g"),
                ingredient("Water", 1, "l"));

        List<RecipeMatcher.AlmostThereRecipe> almost = RecipeMatcher.getAlmostThereRecipes(
                Arrays.asList(pap), pantry(pantryItem("water", 2, "l")));

        assertEquals(1, almost.size());
        assertEquals("Maize meal", almost.get(0).getMissingIngredient().getIngredientName());
        assertEquals(500.0, almost.get(0).getMissingQuantity(), 0.0001);
        assertTrue(almost.get(0).isMissingEntirely());
    }

    @Test
    public void getAlmostThereRecipes_reportsShortfallInRecipeUnit() {
        RecipeWithIngredients bread = recipeWith(1L, "Bread",
                ingredient("Flour", 500, "g"),
                ingredient("Yeast", 1, "unit"));

        List<RecipeMatcher.AlmostThereRecipe> almost = RecipeMatcher.getAlmostThereRecipes(
                Arrays.asList(bread), pantry(pantryItem("flour", 0.3, "kg"), pantryItem("yeast", 1, "unit")));

        assertEquals(1, almost.size());
        assertEquals(200.0, almost.get(0).getMissingQuantity(), 0.0001);
        assertFalse(almost.get(0).isMissingEntirely());
    }

    @Test
    public void getAlmostThereRecipes_excludesCookableAndTwoShortRecipes() {
        RecipeWithIngredients cookable = recipeWith(1L, "Cookable", ingredient("Onion", 1, "unit"));
        RecipeWithIngredients twoShort = recipeWith(2L, "Two short",
                ingredient("Onion", 1, "unit"),
                ingredient("Beef", 1, "kg"),
                ingredient("Tomato", 2, "unit"));

        List<RecipeMatcher.AlmostThereRecipe> almost = RecipeMatcher.getAlmostThereRecipes(
                Arrays.asList(cookable, twoShort), pantry(pantryItem("onion", 1, "unit")));

        assertTrue(almost.isEmpty());
    }

    @Test
    public void getAlmostThereRecipes_neverOverlapsStrictMatches() {
        RecipeWithIngredients a = recipeWith(1L, "A", ingredient("Egg", 2, "unit"));
        RecipeWithIngredients b = recipeWith(2L, "B", ingredient("Egg", 2, "unit"), ingredient("Milk", 250, "ml"));
        List<RecipeWithIngredients> catalogue = Arrays.asList(a, b);
        Map<String, Double> stock = pantry(pantryItem("eggs", 6, "unit"));

        List<RecipeWithIngredients> strict = RecipeMatcher.getMatchingRecipes(catalogue, stock);
        List<RecipeMatcher.AlmostThereRecipe> almost = RecipeMatcher.getAlmostThereRecipes(catalogue, stock);

        assertEquals(Arrays.asList(a), strict);
        assertEquals(1, almost.size());
        assertEquals(b, almost.get(0).getRecipe());
    }

    private static Map<String, Double> pantry(PantryItem... items) {
        return RecipeMatcher.buildPantryQuantityMap(Arrays.asList(items));
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

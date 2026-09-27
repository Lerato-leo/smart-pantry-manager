package za.ac.richfield.dijong.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import za.ac.richfield.dijong.data.dao.RecipeDao;
import za.ac.richfield.dijong.data.entity.Recipe;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;
import za.ac.richfield.dijong.util.RecipeMatcher;
import za.ac.richfield.dijong.util.RecipeSteps;

/**
 * Checks the built-in cookbook against the brief (15-20 recipes, each with a name,
 * ingredients and preparation steps, loaded through Room) and against the rules that keep it
 * useful for strict matching, plus the launch-time check that keeps it complete.
 */
@RunWith(AndroidJUnit4.class)
public class RecipeSeederTest {

    /** The units the seeded recipes may use: what the unit menu offers, in metric. */
    private static final Set<String> ALLOWED_UNITS = new HashSet<>(Arrays.asList(
            "g", "kg", "ml", "L", "tsp", "tbsp", "cup", "unit", "slice", "clove", "can"));

    /** Makes Room's LiveData queries run synchronously, so a test can read the result directly. */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DijongDatabase database;
    private RecipeDao recipeDao;

    @Before
    public void createDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, DijongDatabase.class)
                .allowMainThreadQueries()
                .build();
        recipeDao = database.recipeDao();
    }

    @After
    public void closeDatabase() {
        database.close();
    }

    @Test
    public void seeder_loadsBetweenFifteenAndTwentyRecipes() {
        SouthAfricanRecipeSeeder.populateRecipes(recipeDao);

        int count = recipeDao.countRecipes();
        assertEquals(SouthAfricanRecipeSeeder.RECIPE_COUNT, count);
        assertTrue("expected 15-20 recipes, got " + count, count >= 15 && count <= 20);
    }

    @Test
    public void everyRecipe_hasNameIngredientsAndNumberedSteps() {
        SouthAfricanRecipeSeeder.populateRecipes(recipeDao);

        for (RecipeWithIngredients recipe : allRecipes()) {
            assertFalse("blank name", recipe.getName().trim().isEmpty());
            assertFalse(recipe.getName() + " has no ingredients", recipe.getIngredients().isEmpty());
            assertTrue(recipe.getName() + " needs at least 3 steps",
                    RecipeSteps.split(recipe.getPrepSteps()).size() >= 3);
        }
    }

    @Test
    public void everyIngredient_hasPositiveQuantityAndAKnownUnit() {
        SouthAfricanRecipeSeeder.populateRecipes(recipeDao);

        for (RecipeWithIngredients recipe : allRecipes()) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                String label = recipe.getName() + ": " + ingredient.getIngredientName();
                assertTrue(label + " has no quantity", ingredient.getRequiredQuantity() > 0);
                assertTrue(label + " uses unit " + ingredient.getUnit(), ALLOWED_UNITS.contains(ingredient.getUnit()));
            }
        }
    }

    @Test
    public void noRecipe_listsWaterOrTheSameIngredientTwice() {
        SouthAfricanRecipeSeeder.populateRecipes(recipeDao);

        for (RecipeWithIngredients recipe : allRecipes()) {
            Set<String> seen = new HashSet<>();
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                String name = RecipeMatcher.normalizeIngredientName(ingredient.getIngredientName());
                assertFalse(recipe.getName() + " lists water", RecipeMatcher.isAlwaysAvailable(name));
                assertTrue(recipe.getName() + " lists " + name + " twice", seen.add(name));
            }
        }
    }

    @Test
    public void getRecipeWithIngredients_loadsOneRecipeWithItsIngredients() {
        SouthAfricanRecipeSeeder.populateRecipes(recipeDao);
        RecipeWithIngredients first = allRecipes().get(0);

        RecipeWithIngredients loaded = valueOf(recipeDao.getRecipeWithIngredients(first.getId()));

        assertNotNull(loaded);
        assertEquals(first.getName(), loaded.getName());
        assertEquals(first.getIngredients().size(), loaded.getIngredients().size());
    }

    @Test
    public void ensureRecipesLoaded_loadsAnEmptyDatabase() {
        assertTrue(SouthAfricanRecipeSeeder.ensureRecipesLoaded(database, 0));
        assertEquals(SouthAfricanRecipeSeeder.RECIPE_COUNT, recipeDao.countRecipes());
    }

    @Test
    public void ensureRecipesLoaded_leavesACompleteCurrentCookbookAlone() {
        SouthAfricanRecipeSeeder.ensureRecipesLoaded(database, 0);

        assertFalse(SouthAfricanRecipeSeeder.ensureRecipesLoaded(database, SouthAfricanRecipeSeeder.SEED_VERSION));
        // Running it on every launch must never duplicate the recipes
        assertEquals(SouthAfricanRecipeSeeder.RECIPE_COUNT, recipeDao.countRecipes());
    }

    @Test
    public void ensureRecipesLoaded_repairsAHalfLoadedCookbook() {
        // As if the app was closed partway through its first load
        recipeDao.insertRecipe(new Recipe(0, "Pap and Chakalaka", "1. Cook. 2. Serve. 3. Enjoy."));

        assertTrue(SouthAfricanRecipeSeeder.ensureRecipesLoaded(database, SouthAfricanRecipeSeeder.SEED_VERSION));
        assertEquals(SouthAfricanRecipeSeeder.RECIPE_COUNT, recipeDao.countRecipes());
    }

    @Test
    public void ensureRecipesLoaded_replacesRecipesFromAnOlderVersion() {
        SouthAfricanRecipeSeeder.populateRecipes(recipeDao);

        assertTrue(SouthAfricanRecipeSeeder.ensureRecipesLoaded(database, SouthAfricanRecipeSeeder.SEED_VERSION - 1));
        assertEquals(SouthAfricanRecipeSeeder.RECIPE_COUNT, recipeDao.countRecipes());
    }

    private List<RecipeWithIngredients> allRecipes() {
        return valueOf(recipeDao.getAllRecipesWithIngredients());
    }

    private static <T> T valueOf(LiveData<T> liveData) {
        AtomicReference<T> value = new AtomicReference<>();
        liveData.observeForever(value::set);
        return value.get();
    }
}

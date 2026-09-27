package za.ac.richfield.spens.data;

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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import za.ac.richfield.spens.data.dao.RecipeDao;
import za.ac.richfield.spens.data.entity.RecipeIngredient;

/**
 * Checks the seeded cookbook against the brief: 15-20 recipes, each with a name, at least one
 * ingredient and preparation steps, and loaded together with its ingredients through Room.
 */
@RunWith(AndroidJUnit4.class)
public class RecipeSeederTest {

    /** Makes Room's LiveData queries run synchronously, so a test can read the result directly. */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private SpensDatabase database;
    private RecipeDao recipeDao;

    @Before
    public void createSeededDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, SpensDatabase.class)
                .allowMainThreadQueries()
                .build();
        recipeDao = database.recipeDao();
        SouthAfricanRecipeSeeder.populateRecipes(recipeDao);
    }

    @After
    public void closeDatabase() {
        database.close();
    }

    @Test
    public void seeder_loadsBetweenFifteenAndTwentyRecipes() {
        int count = recipeDao.countRecipes();
        assertTrue("expected 15-20 recipes, got " + count, count >= 15 && count <= 20);
    }

    @Test
    public void everyRecipe_hasNameIngredientsAndSteps() {
        List<RecipeWithIngredients> recipes = valueOf(recipeDao.getAllRecipesWithIngredients());

        assertEquals(recipeDao.countRecipes(), recipes.size());
        for (RecipeWithIngredients recipe : recipes) {
            assertFalse("blank name", recipe.getName().trim().isEmpty());
            assertFalse(recipe.getName() + " has no steps", recipe.getPrepSteps().trim().isEmpty());
            assertFalse(recipe.getName() + " has no ingredients", recipe.getIngredients().isEmpty());
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                assertTrue(recipe.getName() + ": " + ingredient.getIngredientName() + " has no quantity",
                        ingredient.getRequiredQuantity() > 0);
            }
        }
    }

    @Test
    public void getRecipeWithIngredients_loadsOneRecipeWithItsIngredients() {
        RecipeWithIngredients first = valueOf(recipeDao.getAllRecipesWithIngredients()).get(0);

        RecipeWithIngredients loaded = valueOf(recipeDao.getRecipeWithIngredients(first.getId()));

        assertNotNull(loaded);
        assertEquals(first.getName(), loaded.getName());
        assertEquals(first.getIngredients().size(), loaded.getIngredients().size());
    }

    private static <T> T valueOf(LiveData<T> liveData) {
        AtomicReference<T> value = new AtomicReference<>();
        liveData.observeForever(value::set);
        return value.get();
    }
}

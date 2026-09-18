package za.ac.richfield.smartpantry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for managing the SQLite database that stores pantry items and recipes.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database name and version
    private static final String DATABASE_NAME = "smartpantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_PANTRY_ITEMS = "pantry_items";
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    // Pantry Items table columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_UNIT = "unit";
    private static final String COLUMN_EXPIRY_DATE = "expiry_date";

    // Recipes table columns
    private static final String COLUMN_RECIPE_ID = "id";
    private static final String COLUMN_RECIPE_NAME = "name";
    private static final String COLUMN_PREP_STEPS = "prep_steps";

    // Recipe Ingredients table columns
    private static final String COLUMN_RECIPE_INGREDIENT_ID = "id";
    private static final String COLUMN_RECIPE_ID = "recipe_id"; // foreign key to recipes
    private static final String COLUMN_INGREDIENT_NAME = "ingredient_name";
    private static final String COLUMN_REQUIRED_QUANTITY = "required_quantity";
    private static final String COLUMN_RECIPE_UNIT = "unit";

    // SQL statements for creating tables
    private static final String CREATE_TABLE_PANTRY_ITEMS =
            "CREATE TABLE " + TABLE_PANTRY_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " REAL NOT NULL, " +
                    COLUMN_UNIT + " TEXT, " +
                    COLUMN_EXPIRY_DATE + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " (" +
                    COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                    COLUMN_PREP_STEPS + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COLUMN_RECIPE_INGREDIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                    COLUMN_INGREDIENT_NAME + " TEXT NOT NULL, " +
                    COLUMN_REQUIRED_QUANTITY + " REAL NOT NULL, " +
                    COLUMN_RECIPE_UNIT + " TEXT, " +
                    "FOREIGN KEY (" + COLUMN_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_RECIPE_ID + ") ON DELETE CASCADE" +
                    ");";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY_ITEMS);
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENTS);
        Log.d(TAG, "Database tables created");
        // Seed the database with initial recipes
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, we drop the tables and recreate them.
        // In a real app, you might want to handle data migration.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY_ITEMS);
        onCreate(db);
    }

    /**
     * Seed the database with a set of initial recipes.
     * @param db The database to insert into.
     */
    private void seedRecipes(SQLiteDatabase db) {
        // We'll add 15-20 recipes. Here we add 15 for brevity.
        // Each recipe is inserted into the recipes table, then its ingredients into the recipe_ingredients table.

        long recipeId;

        // Recipe 1: Spaghetti Bolognese
        recipeId = insertRecipe(db, "Spaghetti Bolognese", "1. Cook spaghetti according to package directions. 2. In a separate pan, cook ground beef with onion and garlic. 3. Add tomato sauce and simmer. 4. Serve sauce over spaghetti.");
        insertIngredient(db, recipeId, "spaghetti", 200, "grams");
        insertIngredient(db, recipeId, "ground beef", 250, "grams");
        insertIngredient(db, recipeId, "tomato sauce", 500, "ml");
        insertIngredient(db, recipeId, "onion", 1, "unit");
        insertIngredient(db, recipeId, "garlic", 2, "cloves");

        // Recipe 2: Chicken Curry
        recipeId = insertRecipe(db, "Chicken Curry", "1. Fry chicken pieces until browned. 2. Add curry powder, coconut milk, and vegetables. 3. Simmer until chicken is cooked. 4. Serve with rice.");
        insertIngredient(db, recipeId, "chicken breast", 300, "grams");
        insertIngredient(db, recipeId, "curry powder", 2, "tablespoons");
        insertIngredient(db, recipeId, "coconut milk", 400, "ml");
        insertIngredient(db, recipeId, "carrot", 1, "unit");
        insertIngredient(db, recipeId, "potato", 2, "units");

        // Recipe 3: Vegetable Stir Fry
        recipeId = insertRecipe(db, "Vegetable Stir Fry", "1. Heat oil in a wok. 2. Add garlic and ginger. 3. Add mixed vegetables and stir fry. 4. Add soy sauce and serve.");
        insertIngredient(db, recipeId, "mixed vegetables", 300, "grams");
        insertIngredient(db, recipeId, "soy sauce", 2, "tablespoons");
        insertIngredient(db, recipeId, "garlic", 2, "cloves");
        insertIngredient(db, recipeId, "ginger", 1, "teaspoon");
        insertIngredient(db, recipeId, "vegetable oil", 1, "tablespoon");

        // Recipe 4: Pancakes
        recipeId = insertRecipe(db, "Pancakes", "1. Mix flour, eggs, milk, and baking powder. 2. Pour batter onto hot pan. 3. Cook until bubbles form, then flip. 4. Serve with syrup.");
        insertIngredient(db, recipeId, "flour", 200, "grams");
        insertIngredient(db, recipeId, "eggs", 2, "units");
        insertIngredient(db, recipeId, "milk", 300, "ml");
        insertIngredient(db, recipeId, "baking powder", 1, "teaspoon");
        insertIngredient(db, recipeId, "sugar", 1, "tablespoon");

        // Recipe 5: Tomato Soup
        recipeId = insertRecipe(db, "Tomato Soup", "1. Sauté onion and garlic. 2. Add tomatoes and vegetable broth. 3. Simmer and blend. 4. Serve hot.");
        insertIngredient(db, recipeId, "tomatoes", 500, "grams");
        insertIngredient(db, recipeId, "onion", 1, "unit");
        insertIngredient(db, recipeId, "garlic", 2, "cloves");
        insertIngredient(db, recipeId, "vegetable broth", 500, "ml");
        insertIngredient(db, recipeId, "olive oil", 1, "tablespoon");

        // Recipe 6: Grilled Cheese Sandwich
        recipeId = insertRecipe(db, "Grilled Cheese Sandwich", "1. Butter bread slices. 2. Place cheese between slices. 3. Grill until golden brown.");
        insertIngredient(db, recipeId, "bread", 2, "slices");
        insertIngredient(db, recipeId, "cheese", 50, "grams");
        insertIngredient(db, recipeId, "butter", 1, "tablespoon");

        // Recipe 7: Fruit Salad
        recipeId = insertRecipe(db, "Fruit Salad", "1. Chop assorted fruits. 2. Mix with lemon juice and honey. 3. Chill before serving.");
        insertIngredient(db, recipeId, "apple", 1, "unit");
        insertIngredient(db, recipeId, "banana", 1, "unit");
        insertIngredient(db, recipeId, "orange", 1, "unit");
        insertIngredient(db, recipeId, "lemon juice", 1, "tablespoon");
        insertIngredient(db, recipeId, "honey", 1, "tablespoon");

        // Recipe 8: Omelette
        recipeId = insertRecipe(db, "Omelette", "1. Beat eggs with milk, salt, and pepper. 2. Pour into hot pan. 3. Add fillings and fold. 4. Cook until set.");
        insertIngredient(db, recipeId, "eggs", 2, "units");
        insertIngredient(db, recipeId, "milk", 30, "ml");
        insertIngredient(db, recipeId, "cheese", 30, "grams");
        insertIngredient(db, recipeId, "ham", 30, "grams");
        insertIngredient(db, recipeId, "butter", 1, "teaspoon");

        // Recipe 9: Pasta Salad
        recipeId = insertRecipe(db, "Pasta Salad", "1. Cook pasta and let cool. 2. Mix with chopped vegetables and Italian dressing. 3. Chill before serving.");
        insertIngredient(db, recipeId, "pasta", 200, "grams");
        insertIngredient(db, recipeId, "cucumber", 0.5, "unit");
        insertIngredient(db, recipeId, "cherry tomatoes", 100, "grams");
        insertIngredient(db, recipeId, "olives", 30, "grams");
        insertIngredient(db, recipeId, "italian dressing", 2, "tablespoons");

        // Recipe 10: Bean Chili
        recipeId = insertRecipe(db, "Bean Chili", "1. Sauté onion and garlic. 2. Add beans, tomatoes, and spices. 3. Simmer for 30 minutes.");
        insertIngredient(db, recipeId, "kidney beans", 400, "grams");
        insertIngredient(db, recipeId, "black beans", 400, "grams");
        insertIngredient(db, recipeId, "tomatoes", 300, "grams");
        insertIngredient(db, recipeId, "onion", 1, "unit");
        insertIngredient(db, recipeId, "garlic", 2, "cloves");
        insertIngredient(db, recipeId, "chili powder", 1, "tablespoon");

        // Recipe 11: Rice Pudding
        recipeId = insertRecipe(db, "Rice Pudding", "1. Cook rice with milk and sugar until thick. 2. Add cinnamon and raisins. 3. Chill before serving.");
        insertIngredient(db, recipeId, "rice", 100, "grams");
        insertIngredient(db, recipeId, "milk", 500, "ml");
        insertIngredient(db, recipeId, "sugar", 50, "grams");
        insertIngredient(db, recipeId, "cinnamon", 1, "teaspoon");
        insertIngredient(db, recipeId, "raisins", 30, "grams");

        // Recipe 12: Tuna Sandwich
        recipeId = insertRecipe(db, "Tuna Sandwich", "1. Mix tuna with mayo and lemon juice. 2. Spread on bread. 3. Add lettuce and tomato.");
        insertIngredient(db, recipeId, "tuna", 100, "grams");
        insertIngredient(db, recipeId, "mayonnaise", 1, "tablespoon");
        insertIngredient(db, recipeId, "lemon juice", 1, "teaspoon");
        insertIngredient(db, recipeId, "bread", 2, "slices");
        insertIngredient(db, recipeId, "lettuce", 1, "leaf");
        insertIngredient(db, recipeId, "tomato", 0.5, "unit");

        // Recipe 13: Hummus
        recipeId = insertRecipe(db, "Hummus", "1. Blend chickpeas, tahini, lemon juice, and garlic. 2. Add olive oil and salt. 3. Serve with pita bread.");
        insertIngredient(db, recipeId, "chickpeas", 200, "grams");
        insertIngredient(db, recipeId, "tahini", 2, "tablespoons");
        insertIngredient(db, recipeId, "lemon juice", 2, "tablespoons");
        insertIngredient(db, recipeId, "garlic", 2, "cloves");
        insertIngredient(db, recipeId, "olive oil", 1, "tablespoon");

        // Recipe 14: Scrambled Eggs
        recipeId = insertRecipe(db, "Scrambled Eggs", "1. Beat eggs with milk, salt, and pepper. 2. Cook in butter while stirring. 3. Serve hot.");
        insertIngredient(db, recipeId, "eggs", 2, "units");
        insertIngredient(db, recipeId, "milk", 30, "ml");
        insertIngredient(db, recipeId, "butter", 1, "tablespoon");
        insertIngredient(db, recipeId, "salt", 1, "pinch");
        insertIngredient(db, recipeId, "pepper", 1, "pinch");

        // Recipe 15: Lemonade
        recipeId = insertRecipe(db, "Lemonade", "1. Mix lemon juice, water, and sugar. 2. Stir until dissolved. 3. Serve over ice.");
        insertIngredient(db, recipeId, "lemon juice", 100, "ml");
        insertIngredient(db, recipeId, "water", 500, "ml");
        insertIngredient(db, recipeId, "sugar", 50, "grams");
    }

    /**
     * Helper method to insert a recipe and return its ID.
     */
    private long insertRecipe(SQLiteDatabase db, String name, String prepSteps) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPE_NAME, name);
        values.put(COLUMN_PREP_STEPS, prepSteps);
        return db.insert(TABLE_RECIPES, null, values);
    }

    /**
     * Helper method to insert an ingredient for a recipe.
     */
    private void insertIngredient(SQLiteDatabase db, long recipeId, String ingredientName, double quantity, String unit) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPE_ID, recipeId);
        values.put(COLUMN_INGREDIENT_NAME, ingredientName);
        values.put(COLUMN_REQUIRED_QUANTITY, quantity);
        values.put(COLUMN_RECIPE_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, values);
    }

    //region Pantry Items CRUD

    /**
     * Get all pantry items from the database.
     * @return List of PantryItem objects.
     */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> pantryItems = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PANTRY_ITEMS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                PantryItem item = new PantryItem();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)));
                item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT)));
                item.setExpiryDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE)));
                pantryItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return pantryItems;
    }

    /**
     * Insert a pantry item into the database.
     * @param item The PantryItem to insert.
     * @return The row ID of the newly inserted item, or -1 if an error occurred.
     */
    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_QUANTITY, item.getQuantity());
        values.put(COLUMN_UNIT, item.getUnit());
        values.put(COLUMN_EXPIRY_DATE, item.getExpiryDate());

        long id = db.insert(TABLE_PANTRY_ITEMS, null, values);
        db.close();
        return id;
    }

    /**
     * Update a pantry item in the database.
     * @param item The PantryItem to update.
     * @return The number of rows affected.
     */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_QUANTITY, item.getQuantity());
        values.put(COLUMN_UNIT, item.getUnit());
        values.put(COLUMN_EXPIRY_DATE, item.getExpiryDate());

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(item.getId()) };

        int count = db.update(TABLE_PANTRY_ITEMS, values, selection, selectionArgs);
        db.close();
        return count;
    }

    /**
     * Delete a pantry item from the database by its ID.
     * @param id The ID of the item to delete.
     * @return The number of rows deleted.
     */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        int count = db.delete(TABLE_PANTRY_ITEMS, selection, selectionArgs);
        db.close();
        return count;
    }

    //endregion

    //region Recipes

    /**
     * Get all recipes with their ingredients.
     * @return List of Recipe objects, each with its ingredients populated.
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_RECIPES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
                String prepSteps = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREP_STEPS));

                // Get ingredients for this recipe
                List<RecipeIngredient> ingredients = getIngredientsForRecipe(db, recipeId);

                Recipe recipe = new Recipe(recipeId, name, prepSteps, ingredients);
                recipes.add(recipe);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recipes;
    }

    /**
     * Get a recipe by its ID, including its ingredients.
     * @param id The ID of the recipe to retrieve.
     * @return The Recipe object, or null if not found.
     */
    public Recipe getRecipe(long id) {
        Recipe recipe = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_RECIPES + " WHERE " + COLUMN_RECIPE_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{ String.valueOf(id) });

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
            String prepSteps = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREP_STEPS));

            // Get ingredients for this recipe
            List<RecipeIngredient> ingredients = getIngredientsForRecipe(db, id);

            recipe = new Recipe(id, name, prepSteps, ingredients);
        }
        cursor.close();
        db.close();
        return recipe;
    }

    /**
     * Get all ingredients for a given recipe ID.
     * @param db The database to query.
     * @param recipeId The ID of the recipe.
     * @return List of RecipeIngredient objects.
     */
    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RECIPE_INGREDIENTS + " WHERE " + COLUMN_RECIPE_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{ String.valueOf(recipeId) });

        if (cursor.moveToFirst()) {
            do {
                long ingredientId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENT_ID));
                String ingredientName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_NAME));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REQUIRED_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_UNIT));

                RecipeIngredient ingredient = new RecipeIngredient(ingredientId, recipeId, ingredientName, quantity, unit);
                ingredients.add(ingredient);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ingredients;
    }

    /**
     * Get a map of pantry items where the key is the normalized ingredient name and the value is the total quantity.
     * This is used for matching recipes.
     * @return Map of ingredient name (normalized) to quantity.
     */
    public Map<String, Double> getPantryItemQuantities() {
        Map<String, Double> quantities = new HashMap<>();
        List<PantryItem> pantryItems = getAllPantryItems();
        for (PantryItem item : pantryItems) {
            String normalizedName = RecipeMatcher.normalizeIngredientName(item.getName());
            String normalizedUnit = RecipeMatcher.normalizeUnit(item.getUnit());
            // For simplicity, we assume that if the unit is the same (after normalization), we can add quantities.
            // In a more advanced implementation, we might convert between units (e.g., grams to kilograms).
            String key = normalizedName + "#" + normalizedUnit;
            quantities.merge(key, item.getQuantity(), Double::sum);
        }
        return quantities;
    }

    //endregion
}
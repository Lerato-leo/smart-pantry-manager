package za.ac.richfield.dijong.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import za.ac.richfield.dijong.data.dao.PantryDao;
import za.ac.richfield.dijong.data.dao.RecipeDao;
import za.ac.richfield.dijong.data.entity.PantryItem;
import za.ac.richfield.dijong.data.entity.Recipe;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;

@Database(
        entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
        version = 2,
        exportSchema = false
)
public abstract class DijongDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "dijong.db";
    private static final int WRITE_THREAD_POOL_SIZE = 2;

    /** Shared pool for all database writes and background reads (e.g. the expiry-check alarm). */
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(WRITE_THREAD_POOL_SIZE);

    private static volatile DijongDatabase instance;
    private static Context appContext;

    /**
     * Version 2 adds a category to each pantry item. Existing rows keep all their data and
     * are filed under "Other" until the person edits them.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE pantry_items ADD COLUMN category TEXT NOT NULL DEFAULT '"
                    + IngredientCategory.DEFAULT_KEY + "'");
        }
    };

    public abstract PantryDao pantryDao();

    public abstract RecipeDao recipeDao();

    public static DijongDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (DijongDatabase.class) {
                if (instance == null) {
                    appContext = context.getApplicationContext();
                    instance = Room.databaseBuilder(appContext,
                                    DijongDatabase.class, DATABASE_NAME)
                            .addCallback(seedingCallback)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }

    /**
     * Checks the built-in recipes every time the database opens, not just when it's first
     * created: if a first launch was interrupted halfway through loading them, or a newer
     * version of the app ships changed recipes, they're put right on the next launch.
     *
     * <p>The database is only actually opened on first access, which happens after
     * {@link #getInstance} has assigned {@link #instance}, so it's safe to read it here.
     */
    private static final RoomDatabase.Callback seedingCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            databaseWriteExecutor.execute(() -> {
                AppSettings settings = new AppSettings(appContext);
                if (SouthAfricanRecipeSeeder.ensureRecipesLoaded(instance, settings.getLoadedRecipeVersion())) {
                    settings.setLoadedRecipeVersion(SouthAfricanRecipeSeeder.SEED_VERSION);
                }
            });
        }
    };

    /** Reloads the built-in recipes (Settings > Reset sample recipes). Call off the main thread. */
    public void resetSampleRecipes() {
        SouthAfricanRecipeSeeder.replaceAllRecipes(this);
        new AppSettings(appContext).setLoadedRecipeVersion(SouthAfricanRecipeSeeder.SEED_VERSION);
    }
}

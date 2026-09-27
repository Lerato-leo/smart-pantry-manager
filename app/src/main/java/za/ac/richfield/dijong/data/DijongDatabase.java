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
                    instance = Room.databaseBuilder(context.getApplicationContext(),
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
     * The underlying SQLite file is only actually created on first access, which happens
     * lazily after {@link #getInstance} has already assigned {@link #instance}, so it's
     * safe for this callback to read that field directly.
     */
    private static final RoomDatabase.Callback seedingCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> SouthAfricanRecipeSeeder.populateRecipes(instance.recipeDao()));
        }
    };
}

package za.ac.richfield.spens.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import za.ac.richfield.spens.data.dao.PantryDao;
import za.ac.richfield.spens.data.entity.PantryItem;

/**
 * The only door the UI layer has into pantry stock. Every write is handed off to
 * {@link SpensDatabase#databaseWriteExecutor} so a fragment or activity calling in from the
 * main thread never has to wait on SQLite.
 */
public class PantryRepository {

    private final PantryDao pantryDao;

    public PantryRepository(Application application) {
        pantryDao = SpensDatabase.getInstance(application).pantryDao();
    }

    public LiveData<List<PantryItem>> getAllItems() {
        return pantryDao.getAllItems();
    }

    public LiveData<PantryItem> getItemById(long id) {
        return pantryDao.getItemById(id);
    }

    public void insert(PantryItem item) {
        SpensDatabase.databaseWriteExecutor.execute(() -> pantryDao.insert(item));
    }

    public void update(PantryItem item) {
        SpensDatabase.databaseWriteExecutor.execute(() -> pantryDao.update(item));
    }

    public void delete(PantryItem item) {
        SpensDatabase.databaseWriteExecutor.execute(() -> pantryDao.delete(item));
    }
}

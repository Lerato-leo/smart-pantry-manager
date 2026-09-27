package za.ac.richfield.dijong.data;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Consumer;

import za.ac.richfield.dijong.data.dao.PantryDao;
import za.ac.richfield.dijong.data.entity.PantryItem;

/**
 * The only door the UI layer has into pantry stock. Every write is handed off to
 * {@link DijongDatabase#databaseWriteExecutor} so a fragment or activity calling in from the
 * main thread never has to wait on SQLite.
 */
public class PantryRepository {

    private final PantryDao pantryDao;
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public PantryRepository(Application application) {
        pantryDao = DijongDatabase.getInstance(application).pantryDao();
    }

    public LiveData<List<PantryItem>> getAllItems() {
        return pantryDao.getAllItems();
    }

    public LiveData<PantryItem> getItemById(long id) {
        return pantryDao.getItemById(id);
    }

    public void insert(PantryItem item) {
        DijongDatabase.databaseWriteExecutor.execute(() -> pantryDao.insert(item));
    }

    /**
     * Inserts on the background executor, then hands the new row's id back on the main
     * thread, e.g. so the pantry screen can offer Undo on "Ingredient added".
     */
    public void insert(PantryItem item, Consumer<Long> onInserted) {
        DijongDatabase.databaseWriteExecutor.execute(() -> {
            long id = pantryDao.insert(item);
            mainThread.post(() -> onInserted.accept(id));
        });
    }

    public void update(PantryItem item) {
        DijongDatabase.databaseWriteExecutor.execute(() -> pantryDao.update(item));
    }

    public void delete(PantryItem item) {
        DijongDatabase.databaseWriteExecutor.execute(() -> pantryDao.delete(item));
    }

    public void deleteById(long id) {
        DijongDatabase.databaseWriteExecutor.execute(() -> pantryDao.deleteById(id));
    }
}

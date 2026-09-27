package za.ac.richfield.spens.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import za.ac.richfield.spens.data.dao.PantryDao;
import za.ac.richfield.spens.data.entity.PantryItem;

/**
 * Exercises the full Create/Read/Update/Delete cycle on pantry items against a real Room
 * database, held in memory so each test starts empty and leaves nothing behind.
 */
@RunWith(AndroidJUnit4.class)
public class PantryDaoTest {

    private SpensDatabase database;
    private PantryDao pantryDao;

    @Before
    public void createDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, SpensDatabase.class)
                .allowMainThreadQueries()
                .build();
        pantryDao = database.pantryDao();
    }

    @After
    public void closeDatabase() {
        database.close();
    }

    @Test
    public void insert_storesItemAndAssignsId() {
        long id = pantryDao.insert(new PantryItem(0, "Maize meal", 1, "kg", null));

        List<PantryItem> items = pantryDao.getAllItemsSync();
        assertEquals(1, items.size());
        assertEquals(id, items.get(0).getId());
        assertEquals("Maize meal", items.get(0).getName());
        assertEquals(1.0, items.get(0).getQuantity(), 0.0001);
        assertEquals("kg", items.get(0).getUnit());
        assertNull(items.get(0).getExpiryDate());
    }

    @Test
    public void insert_keepsOptionalExpiryDate() {
        pantryDao.insert(new PantryItem(0, "Milk", 1, "l", 20_000L));

        assertEquals(Long.valueOf(20_000L), pantryDao.getAllItemsSync().get(0).getExpiryDate());
    }

    @Test
    public void update_changesExistingRowInPlace() {
        long id = pantryDao.insert(new PantryItem(0, "Onion", 1, "unit", null));

        int updated = pantryDao.update(new PantryItem(id, "Onions", 3, "unit", null));

        List<PantryItem> items = pantryDao.getAllItemsSync();
        assertEquals(1, updated);
        assertEquals(1, items.size());
        assertEquals("Onions", items.get(0).getName());
        assertEquals(3.0, items.get(0).getQuantity(), 0.0001);
    }

    @Test
    public void delete_removesOnlyThatItem() {
        long keepId = pantryDao.insert(new PantryItem(0, "Rice", 500, "g", null));
        long dropId = pantryDao.insert(new PantryItem(0, "Cream", 250, "ml", null));

        int deleted = pantryDao.delete(new PantryItem(dropId, "Cream", 250, "ml", null));

        List<PantryItem> items = pantryDao.getAllItemsSync();
        assertEquals(1, deleted);
        assertEquals(1, items.size());
        assertEquals(keepId, items.get(0).getId());
    }

    @Test
    public void reinsertWithOriginalId_restoresDeletedItem() {
        // What the pantry screen's Undo does after a delete.
        PantryItem item = new PantryItem(0, "Butter", 250, "g", null);
        item.setId(pantryDao.insert(item));
        pantryDao.delete(item);
        assertTrue(pantryDao.getAllItemsSync().isEmpty());

        pantryDao.insert(item);

        List<PantryItem> items = pantryDao.getAllItemsSync();
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }
}

package za.ac.richfield.spens.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import za.ac.richfield.spens.data.entity.PantryItem;

@Dao
public interface PantryDao {

    @Query("SELECT * FROM pantry_items ORDER BY name")
    LiveData<List<PantryItem>> getAllItems();

    /** Synchronous variant for use off the main thread, e.g. from the expiry-check alarm receiver. */
    @Query("SELECT * FROM pantry_items")
    List<PantryItem> getAllItemsSync();

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    LiveData<PantryItem> getItemById(long id);

    @Insert
    long insert(PantryItem item);

    @Update
    int update(PantryItem item);

    @Delete
    int delete(PantryItem item);
}

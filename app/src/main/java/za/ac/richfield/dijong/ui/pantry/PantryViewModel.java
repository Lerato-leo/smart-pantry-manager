package za.ac.richfield.dijong.ui.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import java.util.function.Consumer;

import za.ac.richfield.dijong.data.PantryRepository;
import za.ac.richfield.dijong.data.entity.PantryItem;

/**
 * Backs both the pantry list screen and the add/edit screen. Because the edit screen is a
 * separate {@link android.app.Activity}, its {@code ViewModelProvider} lookup creates its own
 * instance rather than sharing the list screen's. That is fine here, since both instances go
 * through the same {@link PantryRepository}, which is itself backed by the one Room database,
 * so a save on one screen still shows up live on the other.
 */
public class PantryViewModel extends AndroidViewModel {

    private final PantryRepository repository;
    private final LiveData<List<PantryItem>> allItems;
    private final MutableLiveData<Long> selectedItemId = new MutableLiveData<>();
    private final LiveData<PantryItem> selectedItem;

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = new PantryRepository(application);
        allItems = repository.getAllItems();
        selectedItem = Transformations.switchMap(selectedItemId, repository::getItemById);
    }

    public LiveData<List<PantryItem>> getAllItems() {
        return allItems;
    }

    /** Selects the item to expose via {@link #getSelectedItem()}, e.g. when opening the edit screen. */
    public void selectItem(long id) {
        selectedItemId.setValue(id);
    }

    public LiveData<PantryItem> getSelectedItem() {
        return selectedItem;
    }

    public void insert(PantryItem item) {
        repository.insert(item);
    }

    public void insert(PantryItem item, Consumer<Long> onInserted) {
        repository.insert(item, onInserted);
    }

    public void update(PantryItem item) {
        repository.update(item);
    }

    public void delete(PantryItem item) {
        repository.delete(item);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }
}

package za.ac.richfield.smartpantry.ui.pantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.AddEditIngredientActivity;
import za.ac.richfield.smartpantry.db.DatabaseHelper;
import za.ac.richfield.smartpantry.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the list of pantry items.
 * Allows adding, editing, and deleting items.
 */
public class PantryListFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private PantryAdapter adapter;
    private List<PantryItem> pantryItems = new ArrayList<>();

    // Activity result launcher for AddEditIngredientActivity
    private final ActivityResultLauncher<Intent> addEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String name = data.getStringExtra(AddEditIngredientActivity.EXTRA_ITEM_NAME);
                        double quantity = data.getDoubleExtra(AddEditIngredientActivity.EXTRA_ITEM_QUANTITY, 0);
                        String unit = data.getStringExtra(AddEditIngredientActivity.EXTRA_ITEM_UNIT);
                        String expiryDate = data.getStringExtra(AddEditIngredientActivity.EXTRA_ITEM_EXPIRY_DATE);
                        long itemId = data.getLongExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, -1);

                        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
                        if (itemId != -1) {
                            // Updating existing item
                            PantryItem item = new PantryItem();
                            item.setId(itemId);
                            item.setName(name);
                            item.setQuantity(quantity);
                            item.setUnit(unit);
                            item.setExpiryDate(expiryDate);
                            int rows = dbHelper.updatePantryItem(item);
                            if (rows > 0) {
                                Toast.makeText(requireContext(), R.string.msg_ingredient_updated, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to update item", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Inserting new item
                            PantryItem item = new PantryItem();
                            item.setName(name);
                            item.setQuantity(quantity);
                            item.setUnit(unit);
                            item.setExpiryDate(expiryDate);
                            long newId = dbHelper.insertPantryItem(item);
                            if (newId != -1) {
                                Toast.makeText(requireContext(), R.string.msg_ingredient_added, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                            }
                        }
                        dbHelper.close();
                        loadPantryItems();
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_pantry);
        fabAdd = view.findViewById(R.id.fab_add_ingredient);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PantryAdapter(pantryItems,
                item -> editItem(item),
                item -> deleteItem(item));
        recyclerView.setAdapter(adapter);

        // Set up swipe-to-delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                PantryItem item = pantryItems.get(position);
                deleteItem(item);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Set up FAB click
        fabAdd.setOnClickListener(v -> addItem());

        // Load initial data
        loadPantryItems();
    }

    private void loadPantryItems() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        pantryItems = dbHelper.getAllPantryItems();
        dbHelper.close();
        adapter.setPantryItems(pantryItems);
    }

    private void addItem() {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        addEditLauncher.launch(intent);
    }

    private void editItem(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_NAME, item.getName());
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_QUANTITY, item.getQuantity());
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_UNIT, item.getUnit());
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_EXPIRY_DATE, item.getExpiryDate());
        addEditLauncher.launch(intent);
    }

    private void deleteItem(PantryItem item) {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        int rows = dbHelper.deletePantryItem(item.getId());
        dbHelper.close();
        if (rows > 0) {
            Toast.makeText(requireContext(), R.string.msg_ingredient_deleted, Toast.LENGTH_SHORT).show();
            loadPantryItems();
        } else {
            Toast.makeText(requireContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }
}
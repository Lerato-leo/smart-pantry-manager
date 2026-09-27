package za.ac.richfield.spens.ui.pantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import za.ac.richfield.spens.AddEditIngredientActivity;
import za.ac.richfield.spens.R;
import za.ac.richfield.spens.data.entity.PantryItem;

/**
 * Fragment that displays the list of pantry items.
 * Allows adding, editing, and deleting items. All reads/writes go through
 * {@link PantryViewModel}, which keeps this list live-updated whenever the
 * database changes (e.g. after saving in {@link AddEditIngredientActivity}).
 */
public class PantryListFragment extends Fragment {

    private PantryViewModel viewModel;
    private PantryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_pantry);
        View emptyState = view.findViewById(R.id.empty_state);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_ingredient);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PantryAdapter(this::editItem, this::deleteItem);
        recyclerView.setAdapter(adapter);

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
                deleteItem(adapter.getCurrentList().get(position));
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fabAdd.setOnClickListener(v -> addItem());

        viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean isEmpty = items == null || items.isEmpty();
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void addItem() {
        startActivity(new Intent(requireContext(), AddEditIngredientActivity.class));
    }

    private void editItem(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    private void deleteItem(PantryItem item) {
        viewModel.delete(item);
        Toast.makeText(requireContext(), R.string.msg_ingredient_deleted, Toast.LENGTH_SHORT).show();
    }
}

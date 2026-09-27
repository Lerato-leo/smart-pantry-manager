package za.ac.richfield.dijong.ui.pantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import za.ac.richfield.dijong.AddEditIngredientActivity;
import za.ac.richfield.dijong.R;
import za.ac.richfield.dijong.data.entity.PantryItem;
import za.ac.richfield.dijong.util.ExpiryDateConverter;
import za.ac.richfield.dijong.util.PantrySorter;

/**
 * Fragment that displays the list of pantry items, expiring ones first, under a line
 * counting ingredients and how many are expiring soon. Tapping a card edits it (and offers
 * Delete there); swiping a card away deletes it straight away with an Undo on the snackbar.
 * All reads/writes go through
 * {@link PantryViewModel}, which keeps this list live-updated whenever the
 * database changes (e.g. after saving in {@link AddEditIngredientActivity}).
 */
public class PantryListFragment extends Fragment {

    private PantryViewModel viewModel;
    private PantryAdapter adapter;
    private View coordinator;

    /** Opens the add/edit screen and reacts to how it finished (see its RESULT_ codes). */
    private final ActivityResultLauncher<Intent> editorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::onEditorResult);

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
        View pantryContent = view.findViewById(R.id.pantry_content);
        TextView tvIngredientCount = view.findViewById(R.id.tv_ingredient_count);
        TextView tvExpiringSoon = view.findViewById(R.id.tv_expiring_soon);
        coordinator = view.findViewById(R.id.pantry_coordinator);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_ingredient);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PantryAdapter(this::editItem);
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
                if (position != RecyclerView.NO_POSITION) {
                    deleteWithUndo(adapter.getCurrentList().get(position));
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fabAdd.setOnClickListener(v -> addItem());

        viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
            boolean isEmpty = items == null || items.isEmpty();
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            pantryContent.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            if (isEmpty) {
                adapter.submitList(null);
                return;
            }
            long today = ExpiryDateConverter.todayEpochDay();
            adapter.submitList(PantrySorter.sortForDisplay(items, today));
            tvIngredientCount.setText(getResources().getQuantityString(
                    R.plurals.pantry_ingredient_count, items.size(), items.size()));
            int expiringSoon = PantrySorter.countExpiringSoon(items, today);
            tvExpiringSoon.setVisibility(expiringSoon > 0 ? View.VISIBLE : View.GONE);
            tvExpiringSoon.setText(getString(R.string.pantry_expiring_soon, expiringSoon));
        });
    }

    private void addItem() {
        editorLauncher.launch(new Intent(requireContext(), AddEditIngredientActivity.class));
    }

    private void editItem(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        editorLauncher.launch(intent);
    }

    private void onEditorResult(ActivityResult result) {
        Intent data = result.getData();
        long id = data != null ? data.getLongExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, -1) : -1;
        if (result.getResultCode() == AddEditIngredientActivity.RESULT_ADDED && id != -1) {
            Snackbar.make(coordinator, R.string.msg_ingredient_added, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_undo, v -> viewModel.deleteById(id))
                    .show();
        } else if (result.getResultCode() == AddEditIngredientActivity.RESULT_UPDATED) {
            Snackbar.make(coordinator, R.string.msg_ingredient_updated, Snackbar.LENGTH_SHORT).show();
        } else if (result.getResultCode() == AddEditIngredientActivity.RESULT_DELETE_REQUESTED) {
            for (PantryItem item : adapter.getCurrentList()) {
                if (item.getId() == id) {
                    deleteWithUndo(item);
                    break;
                }
            }
        }
    }

    /**
     * Deletes immediately and offers Undo. Undo re-inserts the same object, id included, so
     * the restored row is identical to the one that was removed.
     */
    private void deleteWithUndo(PantryItem item) {
        viewModel.delete(item);
        Snackbar.make(coordinator, getString(R.string.msg_ingredient_deleted, item.getName()),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, v -> viewModel.insert(item))
                .show();
    }
}

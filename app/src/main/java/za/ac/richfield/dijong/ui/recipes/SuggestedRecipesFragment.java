package za.ac.richfield.dijong.ui.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import za.ac.richfield.dijong.AddEditIngredientActivity;
import za.ac.richfield.dijong.MainActivity;
import za.ac.richfield.dijong.R;
import za.ac.richfield.dijong.RecipeDetailActivity;
import za.ac.richfield.dijong.data.AppSettings;
import za.ac.richfield.dijong.data.RecipeWithIngredients;
import za.ac.richfield.dijong.util.RecipeMatcher.AlmostThereRecipe;

/**
 * Fragment that displays recipes that match the current pantry ingredients.
 * Uses a strict-matching rule: only recipes where every required ingredient
 * is present in sufficient quantity are shown as ready to cook. Recipes one
 * ingredient short go in a separate amber "Almost there" panel below (unless
 * turned off in Settings). With nothing in either, a full empty state explains
 * why and offers to add ingredients. Everything updates whenever the pantry or
 * catalogue changes.
 */
public class SuggestedRecipesFragment extends Fragment {

    private View recipesScroll;
    private View emptyState;
    private TextView tvReadyCount;
    private TextView tvNoneReady;
    private View almostThereSection;
    private RecipeAdapter adapter;
    private AlmostThereAdapter almostThereAdapter;
    private boolean showAlmostThere;

    private List<RecipeWithIngredients> readyRecipes = Collections.emptyList();
    private List<AlmostThereRecipe> almostThereRecipes = Collections.emptyList();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recipesScroll = view.findViewById(R.id.recipes_scroll);
        emptyState = view.findViewById(R.id.empty_state);
        tvReadyCount = view.findViewById(R.id.tv_ready_count);
        tvNoneReady = view.findViewById(R.id.tv_none_ready);
        almostThereSection = view.findViewById(R.id.almost_there_section);
        RecyclerView readyList = view.findViewById(R.id.rv_suggested_recipes);
        RecyclerView almostThereList = view.findViewById(R.id.rv_almost_there);

        readyList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeAdapter(this::openRecipeDetail);
        readyList.setAdapter(adapter);

        almostThereList.setLayoutManager(new LinearLayoutManager(requireContext()));
        almostThereAdapter = new AlmostThereAdapter(this::openRecipeDetail,
                new AppSettings(requireContext()).isImperialUnitsEnabled());
        almostThereList.setAdapter(almostThereAdapter);

        view.findViewById(R.id.btn_add_ingredients).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));

        showAlmostThere = new AppSettings(requireContext()).isAlmostThereShown();

        RecipeViewModel viewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        viewModel.getMatchingRecipes().observe(getViewLifecycleOwner(), recipes -> {
            readyRecipes = recipes != null ? recipes : Collections.emptyList();
            render();
        });
        viewModel.getAlmostThereRecipes().observe(getViewLifecycleOwner(), recipes -> {
            almostThereRecipes = recipes != null ? recipes : Collections.emptyList();
            render();
        });
        viewModel.getPantryItemCount().observe(getViewLifecycleOwner(), this::showPantryCount);
    }

    private void render() {
        List<AlmostThereRecipe> almostShown = showAlmostThere ? almostThereRecipes : Collections.emptyList();
        boolean nothingAtAll = readyRecipes.isEmpty() && almostShown.isEmpty();
        emptyState.setVisibility(nothingAtAll ? View.VISIBLE : View.GONE);
        recipesScroll.setVisibility(nothingAtAll ? View.GONE : View.VISIBLE);

        adapter.submitList(readyRecipes);
        tvReadyCount.setText(getResources().getQuantityString(
                R.plurals.recipe_count, readyRecipes.size(), readyRecipes.size()));
        // Nothing strictly cookable but something close: say so, rather than an empty heading
        tvNoneReady.setVisibility(readyRecipes.isEmpty() ? View.VISIBLE : View.GONE);

        almostThereAdapter.submitList(almostShown);
        almostThereSection.setVisibility(almostShown.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showPantryCount(Integer count) {
        if (count == null || !(getActivity() instanceof MainActivity) || !isVisible()) {
            return;
        }
        ((MainActivity) getActivity()).setScreenSubtitle(
                getResources().getQuantityString(R.plurals.cook_subtitle, count, count));
    }

    private void openRecipeDetail(RecipeWithIngredients recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}

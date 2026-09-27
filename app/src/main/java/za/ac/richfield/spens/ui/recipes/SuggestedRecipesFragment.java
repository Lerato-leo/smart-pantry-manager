package za.ac.richfield.spens.ui.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import za.ac.richfield.spens.R;
import za.ac.richfield.spens.RecipeDetailActivity;
import za.ac.richfield.spens.data.RecipeWithIngredients;

/**
 * Fragment that displays recipes that match the current pantry ingredients.
 * Uses a strict-matching rule: only recipes where every required ingredient
 * is present in sufficient quantity are shown. The list is reactive: it
 * updates automatically whenever the pantry or recipe catalog changes.
 */
public class SuggestedRecipesFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private RecipeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_suggested_recipes);
        emptyState = view.findViewById(R.id.empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeAdapter(this::openRecipeDetail);
        recyclerView.setAdapter(adapter);

        RecipeViewModel viewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        viewModel.getMatchingRecipes().observe(getViewLifecycleOwner(), this::showMatchingRecipes);
    }

    private void showMatchingRecipes(List<RecipeWithIngredients> matchingRecipes) {
        adapter.submitList(matchingRecipes);
        boolean isEmpty = matchingRecipes == null || matchingRecipes.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openRecipeDetail(RecipeWithIngredients recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}

package za.ac.richfield.smartpantry.ui.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.RecipeDetailActivity;
import za.ac.richfield.smartpantry.db.DatabaseHelper;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment that displays recipes that match the current pantry ingredients.
 * Uses a strict-matching rule: only recipes where every required ingredient
 * is present in sufficient quantity are shown.
 */
public class SuggestedRecipesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private RecipeAdapter adapter;
    private List<Recipe> allRecipes = new ArrayList<>();
    private List<Recipe> matchingRecipes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_suggested_recipes);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeAdapter(matchingRecipes, recipe -> openRecipeDetail(recipe));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMatchingRecipes();
    }

    private void loadMatchingRecipes() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        // Get all recipes
        allRecipes = dbHelper.getAllRecipes();
        // Get pantry items and create a map for matching
        List<PantryItem> pantryItems = dbHelper.getAllPantryItems();
        Map<String, Double> pantryMap = new HashMap<>();
        for (PantryItem item : pantryItems) {
            String normName = za.ac.richfield.smartpantry.util.RecipeMatcher.normalizeIngredientName(item.getName());
            String normUnit = za.ac.richfield.smartpantry.util.RecipeMatcher.normalizeUnit(item.getUnit());
            String key = normName + "#" + normUnit;
            pantryMap.merge(key, item.getQuantity(), Double::sum);
        }
        dbHelper.close();

        // Get matching recipes
        matchingRecipes = za.ac.richfield.smartpantry.util.RecipeMatcher.getMatchingRecipes(allRecipes, pantryMap);
        adapter.setRecipes(matchingRecipes);

        // Show empty state if no matches
        if (matchingRecipes.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
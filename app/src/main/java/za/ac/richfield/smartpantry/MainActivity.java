package za.ac.richfield.smartpantry;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import za.ac.richfield.smartpantry.ui.pantry.PantryListFragment;
import za.ac.richfield.smartpantry.ui.recipes.SuggestedRecipesFragment;
import za.ac.richfield.smartpantry.ui.settings.SettingsFragment;

/**
 * Main activity that hosts the bottom navigation and fragments.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_pantry) {
                selectedFragment = new PantryListFragment();
            } else if (itemId == R.id.navigation_recipes) {
                selectedFragment = new SuggestedRecipesFragment();
            } else if (itemId == R.id.navigation_settings) {
                selectedFragment = new SettingsFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new PantryListFragment())
                    .commit();
        }
    }
}
package za.ac.richfield.spens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import za.ac.richfield.spens.ui.pantry.PantryListFragment;
import za.ac.richfield.spens.ui.recipes.SuggestedRecipesFragment;
import za.ac.richfield.spens.ui.settings.SettingsFragment;

/**
 * Main activity that hosts the bottom navigation and fragments.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            int titleResId = 0;
            if (itemId == R.id.navigation_pantry) {
                selectedFragment = new PantryListFragment();
                titleResId = R.string.title_pantry;
            } else if (itemId == R.id.navigation_recipes) {
                selectedFragment = new SuggestedRecipesFragment();
                titleResId = R.string.title_recipes;
            } else if (itemId == R.id.navigation_settings) {
                selectedFragment = new SettingsFragment();
                titleResId = R.string.title_settings;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                showScreenTitle(titleResId);
                return true;
            }
            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new PantryListFragment())
                    .commit();
            showScreenTitle(R.string.title_pantry);
        }
    }

    /**
     * Sets the toolbar title, showing the app's tagline as a subtitle only on the home
     * (pantry) tab so it doesn't repeat and clutter the other two screens.
     */
    private void showScreenTitle(int titleResId) {
        setTitle(titleResId);
        if (getSupportActionBar() != null) {
            boolean isHomeTab = titleResId == R.string.title_pantry;
            getSupportActionBar().setSubtitle(isHomeTab ? R.string.app_tagline : 0);
        }
    }
}
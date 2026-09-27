package za.ac.richfield.dijong;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import za.ac.richfield.dijong.ui.pantry.PantryListFragment;
import za.ac.richfield.dijong.ui.recipes.SuggestedRecipesFragment;
import za.ac.richfield.dijong.ui.settings.SettingsFragment;

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
                showScreenTitle(itemId);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new PantryListFragment())
                    .commit();
        }
        // After a rotation the fragment is restored by the system but the toolbar isn't,
        // so the title is always set from whichever tab is selected.
        showScreenTitle(bottomNavigationView.getSelectedItemId());
    }

    /**
     * Sets the toolbar title for a tab, with the app's tagline as a subtitle only on the
     * home (pantry) tab. What Can I Cook fills in its own subtitle once it knows how many
     * ingredients there are, via {@link #setScreenSubtitle}.
     */
    private void showScreenTitle(int tabItemId) {
        int titleResId;
        if (tabItemId == R.id.navigation_recipes) {
            titleResId = R.string.title_recipes;
        } else if (tabItemId == R.id.navigation_settings) {
            titleResId = R.string.title_settings;
        } else {
            titleResId = R.string.title_pantry;
        }
        setTitle(titleResId);
        setScreenSubtitle(titleResId == R.string.title_pantry ? getString(R.string.app_tagline) : null);
    }

    /** Lets the hosted tab put a live line under the title, e.g. an ingredient count. */
    public void setScreenSubtitle(CharSequence subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
}
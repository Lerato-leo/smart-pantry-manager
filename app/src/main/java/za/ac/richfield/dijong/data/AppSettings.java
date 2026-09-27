package za.ac.richfield.dijong.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The person's choices from the Settings screen, kept in SharedPreferences since they're a
 * handful of flags rather than data worth a database table.
 */
public final class AppSettings {

    public static final String PREFS_NAME = "DijongPrefs";
    public static final String KEY_EXPIRING_SOON_ALERTS = "expiring_soon_alerts";
    public static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";
    public static final String KEY_IMPERIAL_UNITS = "imperial_units";
    public static final String KEY_LOADED_RECIPE_VERSION = "loaded_recipe_version";

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isExpiringSoonAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRING_SOON_ALERTS, false);
    }

    public void setExpiringSoonAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRING_SOON_ALERTS, enabled).apply();
    }

    /** On by default: the bonus list only shows up if there's something in it anyway. */
    public boolean isAlmostThereShown() {
        return prefs.getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public void setAlmostThereShown(boolean shown) {
        prefs.edit().putBoolean(KEY_SHOW_ALMOST_THERE, shown).apply();
    }

    /**
     * True for imperial, false (the default) for metric: South African kitchens measure in
     * metric. It's one or the other, never both; see {@link za.ac.richfield.dijong.util.UnitSystem}.
     */
    public boolean isImperialUnitsEnabled() {
        return prefs.getBoolean(KEY_IMPERIAL_UNITS, false);
    }

    public void setImperialUnitsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_IMPERIAL_UNITS, enabled).apply();
    }

    /** Which version of the built-in recipes is in the database; 0 before the first load. */
    public int getLoadedRecipeVersion() {
        return prefs.getInt(KEY_LOADED_RECIPE_VERSION, 0);
    }

    public void setLoadedRecipeVersion(int version) {
        // commit(), not apply(): this runs on a background thread right after the reload, and
        // the version must be on disk before the app can be killed, or it would reload again.
        prefs.edit().putInt(KEY_LOADED_RECIPE_VERSION, version).commit();
    }
}

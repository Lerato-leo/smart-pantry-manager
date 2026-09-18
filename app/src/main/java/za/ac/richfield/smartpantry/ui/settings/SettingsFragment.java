package za.ac.richfield.smartpantry.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import za.ac.richfield.smartpantry.R;

/**
 * Fragment for application settings.
 * Currently includes a toggle for expiring-soon alerts.
 */
public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "SmartPantryPrefs";
    private static final String KEY_EXPIRING_SOON_ALERTS = "expiring_soon_alerts";

    private Switch switchExpiringSoon;
    private TextView tvExpiringSoonSummary;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchExpiringSoon = view.findViewById(R.id.switch_expiring_soon_alerts);
        tvExpiringSoonSummary = view.findViewById(R.id.tv_expiring_soon_summary);

        // Load saved state
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        boolean expiringSoonEnabled = prefs.getBoolean(KEY_EXPIRING_SOON_ALERTS, false);
        switchExpiringSoon.setChecked(expiringSoonEnabled);

        // Set up listener for changes
        switchExpiringSoon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_EXPIRING_SOON_ALERTS, isChecked);
            editor.apply();
            // Optionally, show a toast or update summary
            tvExpiringSoonSummary.setText(isChecked ?
                    R.string.pref_expiring_soon_alerts_summary :
                    getString(R.string.pref_expiring_soon_alerts_summary));
        });

        // Initialize summary text
        tvExpiringSoonSummary.setText(expiringSoonEnabled ?
                R.string.pref_expiring_soon_alerts_summary :
                getString(R.string.pref_expiring_soon_alerts_summary));
    }
}
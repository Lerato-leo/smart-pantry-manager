package za.ac.richfield.smartpantry.ui.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.notify.ExpiryAlertScheduler;

/**
 * Fragment for application settings.
 * Currently includes a toggle for expiring-soon alerts.
 */
public class SettingsFragment extends Fragment {

    public static final String PREFS_NAME = "SmartPantryPrefs";
    public static final String KEY_EXPIRING_SOON_ALERTS = "expiring_soon_alerts";

    private Switch switchExpiringSoon;
    private TextView tvExpiringSoonSummary;
    private SharedPreferences prefs;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private final CompoundButton.OnCheckedChangeListener checkedChangeListener =
            (buttonView, isChecked) -> {
                if (isChecked && !hasNotificationPermission()) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    setExpiringSoonAlertsEnabled(isChecked);
                }
            };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> setExpiringSoonAlertsEnabled(granted));
    }

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

        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        boolean expiringSoonEnabled = prefs.getBoolean(KEY_EXPIRING_SOON_ALERTS, false);
        switchExpiringSoon.setChecked(expiringSoonEnabled);

        switchExpiringSoon.setOnCheckedChangeListener(checkedChangeListener);

        tvExpiringSoonSummary.setText(getString(R.string.pref_expiring_soon_alerts_summary));
    }

    /**
     * Persists the toggle state, (re)schedules or cancels the daily expiry check,
     * and keeps the switch UI in sync with the outcome (e.g. if permission was denied).
     */
    private void setExpiringSoonAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRING_SOON_ALERTS, enabled).apply();
        if (switchExpiringSoon.isChecked() != enabled) {
            switchExpiringSoon.setOnCheckedChangeListener(null);
            switchExpiringSoon.setChecked(enabled);
            switchExpiringSoon.setOnCheckedChangeListener(checkedChangeListener);
        }
        if (enabled) {
            ExpiryAlertScheduler.schedule(requireContext());
        } else {
            ExpiryAlertScheduler.cancel(requireContext());
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}

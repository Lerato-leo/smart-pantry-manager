package za.ac.richfield.dijong.ui.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;

import za.ac.richfield.dijong.R;
import za.ac.richfield.dijong.data.AppSettings;
import za.ac.richfield.dijong.data.RecipeRepository;
import za.ac.richfield.dijong.notify.ExpiryAlertScheduler;

/**
 * Settings: expiring-soon alerts, whether What Can I Cook shows the Almost There list,
 * whether the unit menu offers imperial units, and a button to restore the built-in recipes.
 * Choices are saved in {@link AppSettings} as soon as they're made.
 */
public class SettingsFragment extends Fragment {

    private AppSettings settings;
    private MaterialSwitch switchExpiringSoon;
    private MaterialRadioButton radioMetric;
    private MaterialRadioButton radioImperial;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private final CompoundButton.OnCheckedChangeListener alertsChangeListener =
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
        settings = new AppSettings(requireContext());

        switchExpiringSoon = view.findViewById(R.id.switch_expiring_soon_alerts);
        switchExpiringSoon.setChecked(settings.isExpiringSoonAlertsEnabled());
        switchExpiringSoon.setOnCheckedChangeListener(alertsChangeListener);
        view.findViewById(R.id.row_expiring_soon_alerts).setOnClickListener(v -> switchExpiringSoon.toggle());

        MaterialSwitch switchAlmostThere = view.findViewById(R.id.switch_show_almost_there);
        switchAlmostThere.setChecked(settings.isAlmostThereShown());
        switchAlmostThere.setOnCheckedChangeListener((buttonView, isChecked) -> settings.setAlmostThereShown(isChecked));
        view.findViewById(R.id.row_show_almost_there).setOnClickListener(v -> switchAlmostThere.toggle());

        radioMetric = view.findViewById(R.id.radio_units_metric);
        radioImperial = view.findViewById(R.id.radio_units_imperial);
        showUnitsChoice(settings.isImperialUnitsEnabled());
        view.findViewById(R.id.row_units_metric).setOnClickListener(v -> chooseImperial(false));
        view.findViewById(R.id.row_units_imperial).setOnClickListener(v -> chooseImperial(true));

        MaterialButton btnReset = view.findViewById(R.id.btn_reset_recipes);
        btnReset.setOnClickListener(v -> resetSampleRecipes(btnReset));
    }

    private void chooseImperial(boolean imperial) {
        settings.setImperialUnitsEnabled(imperial);
        showUnitsChoice(imperial);
    }

    private void showUnitsChoice(boolean imperial) {
        radioMetric.setChecked(!imperial);
        radioImperial.setChecked(imperial);
    }

    private void resetSampleRecipes(MaterialButton button) {
        button.setEnabled(false);
        new RecipeRepository(requireActivity().getApplication()).resetSampleRecipes(() -> {
            if (getView() == null) {
                return;
            }
            button.setEnabled(true);
            // Anchored above the bottom navigation so it doesn't cover the tabs
            Snackbar.make(getView(), R.string.msg_recipes_reset, Snackbar.LENGTH_SHORT)
                    .setAnchorView(requireActivity().findViewById(R.id.nav_view))
                    .show();
        });
    }

    /**
     * Persists the toggle state, (re)schedules or cancels the daily expiry check,
     * and keeps the switch UI in sync with the outcome (e.g. if permission was denied).
     */
    private void setExpiringSoonAlertsEnabled(boolean enabled) {
        settings.setExpiringSoonAlertsEnabled(enabled);
        if (switchExpiringSoon.isChecked() != enabled) {
            switchExpiringSoon.setOnCheckedChangeListener(null);
            switchExpiringSoon.setChecked(enabled);
            switchExpiringSoon.setOnCheckedChangeListener(alertsChangeListener);
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

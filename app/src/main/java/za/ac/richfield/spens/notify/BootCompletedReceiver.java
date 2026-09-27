package za.ac.richfield.spens.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import za.ac.richfield.spens.ui.settings.SettingsFragment;

/**
 * Reschedules the expiry-alert alarm after a device reboot, since AlarmManager
 * alarms are cleared when the device restarts.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(
                SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE);
        boolean expiringSoonEnabled = prefs.getBoolean(
                SettingsFragment.KEY_EXPIRING_SOON_ALERTS, false);
        if (expiringSoonEnabled) {
            ExpiryAlertScheduler.schedule(context);
        }
    }
}

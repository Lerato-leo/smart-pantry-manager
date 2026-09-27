package za.ac.richfield.dijong.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import za.ac.richfield.dijong.data.AppSettings;

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
        if (new AppSettings(context).isExpiringSoonAlertsEnabled()) {
            ExpiryAlertScheduler.schedule(context);
        }
    }
}

package za.ac.richfield.smartpantry.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Schedules and cancels the recurring daily check for pantry items that are expiring soon.
 */
public final class ExpiryAlertScheduler {

    private static final int REQUEST_CODE = 1001;
    private static final int CHECK_HOUR_OF_DAY = 9;

    private ExpiryAlertScheduler() {
    }

    public static void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = buildPendingIntent(context);

        Calendar firstTrigger = Calendar.getInstance();
        firstTrigger.set(Calendar.HOUR_OF_DAY, CHECK_HOUR_OF_DAY);
        firstTrigger.set(Calendar.MINUTE, 0);
        firstTrigger.set(Calendar.SECOND, 0);
        if (firstTrigger.getTimeInMillis() <= System.currentTimeMillis()) {
            firstTrigger.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                firstTrigger.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(buildPendingIntent(context));
    }

    private static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, ExpiryCheckReceiver.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}

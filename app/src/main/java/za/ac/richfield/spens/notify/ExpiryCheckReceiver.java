package za.ac.richfield.spens.notify;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.spens.MainActivity;
import za.ac.richfield.spens.R;
import za.ac.richfield.spens.data.SpensDatabase;
import za.ac.richfield.spens.data.entity.PantryItem;
import za.ac.richfield.spens.util.ExpiryDateConverter;

/**
 * Runs once a day (see {@link ExpiryAlertScheduler}) and posts one notification listing every
 * stock item expiring within {@link ExpiryDateConverter#EXPIRY_SOON_WINDOW_DAYS} days, rather
 * than spamming a separate notification per item.
 */
public class ExpiryCheckReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "expiring_soon_alerts";
    private static final int NOTIFICATION_ID = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        PendingResult pendingResult = goAsync();
        SpensDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<PantryItem> expiringItems = findExpiringItems(appContext);
                if (!expiringItems.isEmpty()) {
                    postNotification(appContext, expiringItems);
                }
            } finally {
                pendingResult.finish();
            }
        });
    }

    private List<PantryItem> findExpiringItems(Context context) {
        long today = ExpiryDateConverter.todayEpochDay();
        long cutoff = today + ExpiryDateConverter.EXPIRY_SOON_WINDOW_DAYS;

        List<PantryItem> aboutToGoOff = new ArrayList<>();
        List<PantryItem> wholeSpens = SpensDatabase.getInstance(context).pantryDao().getAllItemsSync();
        for (PantryItem candidate : wholeSpens) {
            Long expiryDate = candidate.getExpiryDate();
            if (expiryDate != null && expiryDate >= today && expiryDate <= cutoff) {
                aboutToGoOff.add(candidate);
            }
        }
        return aboutToGoOff;
    }

    private void postNotification(Context context, List<PantryItem> expiringItems) {
        createNotificationChannel(context);

        StringBuilder itemNamesJoined = new StringBuilder();
        for (int i = 0; i < expiringItems.size(); i++) {
            if (i > 0) {
                itemNamesJoined.append(", ");
            }
            itemNamesJoined.append(expiringItems.get(i).getName());
        }

        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pantry)
                .setContentTitle(context.getString(R.string.notif_expiring_soon_title))
                .setContentText(itemNamesJoined.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(itemNamesJoined.toString()))
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null || notificationManager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.pref_expiring_soon_alerts_summary));
        notificationManager.createNotificationChannel(channel);
    }
}

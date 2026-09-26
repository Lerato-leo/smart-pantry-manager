package za.ac.richfield.smartpantry.notify;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import za.ac.richfield.smartpantry.MainActivity;
import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.db.DatabaseHelper;
import za.ac.richfield.smartpantry.model.PantryItem;

/**
 * Checks the pantry for items expiring within {@link #EXPIRY_WINDOW_DAYS} days
 * and posts a single summary notification when any are found.
 */
public class ExpiryCheckReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "expiring_soon_alerts";
    private static final int NOTIFICATION_ID = 2001;
    private static final int EXPIRY_WINDOW_DAYS = 3;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public void onReceive(Context context, Intent intent) {
        List<PantryItem> expiringItems = findExpiringItems(context);
        if (expiringItems.isEmpty()) {
            return;
        }
        postNotification(context, expiringItems);
    }

    private List<PantryItem> findExpiringItems(Context context) {
        List<PantryItem> expiringItems = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.DAY_OF_YEAR, EXPIRY_WINDOW_DAYS);
        Date cutoffDate = endOfDay(cutoff.getTime());
        Date today = startOfDay(new Date());

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<PantryItem> allItems = dbHelper.getAllPantryItems();
        dbHelper.close();

        for (PantryItem item : allItems) {
            String expiryDate = item.getExpiryDate();
            if (expiryDate == null || expiryDate.isEmpty()) {
                continue;
            }
            try {
                Date parsed = dateFormat.parse(expiryDate);
                if (parsed != null && !parsed.before(today) && !parsed.after(cutoffDate)) {
                    expiringItems.add(item);
                }
            } catch (ParseException e) {
                // Skip items with an unparseable expiry date.
            }
        }
        return expiringItems;
    }

    private void postNotification(Context context, List<PantryItem> expiringItems) {
        createNotificationChannel(context);

        StringBuilder names = new StringBuilder();
        for (int i = 0; i < expiringItems.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            names.append(expiringItems.get(i).getName());
        }

        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pantry)
                .setContentTitle(context.getString(R.string.notif_expiring_soon_title))
                .setContentText(names.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(names.toString()))
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

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date endOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}

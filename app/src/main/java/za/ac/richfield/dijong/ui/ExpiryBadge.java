package za.ac.richfield.dijong.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import za.ac.richfield.dijong.R;
import za.ac.richfield.dijong.util.ExpiryStatus;

/**
 * Fills in the rounded expiry badge: amber with a clock for anything expiring within the
 * soon window, red for anything past its date, and hidden otherwise. Pantry cards use the
 * short wording ("Tomorrow"), the edit screen the long one ("Expires tomorrow").
 */
public final class ExpiryBadge {

    private ExpiryBadge() {
    }

    public static void bind(TextView badge, ExpiryStatus status, boolean longWording) {
        Context context = badge.getContext();
        if (!status.needsAttention()) {
            badge.setVisibility(View.GONE);
            return;
        }
        badge.setVisibility(View.VISIBLE);

        boolean expired = status.getKind() == ExpiryStatus.Kind.EXPIRED;
        int background = expired ? R.color.overdue : R.color.amber;
        int foreground = expired ? R.color.white : R.color.ink;
        badge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, background)));
        badge.setTextColor(ContextCompat.getColor(context, foreground));
        TextViewCompat.setCompoundDrawableTintList(badge,
                ColorStateList.valueOf(ContextCompat.getColor(context, foreground)));
        badge.setText(label(context, status, longWording));
    }

    private static String label(Context context, ExpiryStatus status, boolean longWording) {
        if (status.getKind() == ExpiryStatus.Kind.EXPIRED) {
            return context.getString(R.string.expired);
        }
        int days = (int) status.getDaysLeft();
        if (days == 0) {
            return context.getString(longWording ? R.string.expires_today : R.string.badge_today);
        }
        if (days == 1) {
            return context.getString(longWording ? R.string.expires_tomorrow : R.string.badge_tomorrow);
        }
        int plural = longWording ? R.plurals.expires_in_days : R.plurals.badge_in_days;
        return context.getResources().getQuantityString(plural, days, days);
    }
}

package za.ac.richfield.dijong.util;

import androidx.annotation.Nullable;

/**
 * Where an ingredient sits relative to its expiry date, for the badges on pantry cards
 * ("Tomorrow", "In 2 days", "Expired") and the "N expiring soon" count above the list.
 */
public final class ExpiryStatus {

    public enum Kind {
        /** No expiry date recorded. */
        NONE,
        /** Past its date. */
        EXPIRED,
        /** Expires today or within {@link ExpiryDateConverter#EXPIRY_SOON_WINDOW_DAYS} days. */
        SOON,
        /** Further off than the soon window; no badge is shown. */
        FRESH
    }

    private final Kind kind;
    private final long daysLeft;

    private ExpiryStatus(Kind kind, long daysLeft) {
        this.kind = kind;
        this.daysLeft = daysLeft;
    }

    public static ExpiryStatus of(@Nullable Long expiryEpochDay, long todayEpochDay) {
        if (expiryEpochDay == null) {
            return new ExpiryStatus(Kind.NONE, 0);
        }
        long daysLeft = expiryEpochDay - todayEpochDay;
        if (daysLeft < 0) {
            return new ExpiryStatus(Kind.EXPIRED, daysLeft);
        }
        if (daysLeft <= ExpiryDateConverter.EXPIRY_SOON_WINDOW_DAYS) {
            return new ExpiryStatus(Kind.SOON, daysLeft);
        }
        return new ExpiryStatus(Kind.FRESH, daysLeft);
    }

    public static ExpiryStatus of(@Nullable Long expiryEpochDay) {
        return of(expiryEpochDay, ExpiryDateConverter.todayEpochDay());
    }

    public Kind getKind() {
        return kind;
    }

    /** Days from today to the expiry date: 0 is today, 1 tomorrow, negative once expired. */
    public long getDaysLeft() {
        return daysLeft;
    }

    /** True for anything the person should act on now: expired or expiring soon. */
    public boolean needsAttention() {
        return kind == Kind.EXPIRED || kind == Kind.SOON;
    }
}

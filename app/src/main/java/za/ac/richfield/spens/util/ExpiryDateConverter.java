package za.ac.richfield.spens.util;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converts between the epoch-day {@code Long} used to store expiry dates and the
 * yyyy-MM-dd string shown in the UI.
 */
public final class ExpiryDateConverter {

    /** Items expiring within this many days are flagged "soon" in the UI and in alerts. */
    public static final int EXPIRY_SOON_WINDOW_DAYS = 3;

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private ExpiryDateConverter() {
    }

    /**
     * @return the epoch day for a yyyy-MM-dd string, or null if the input is blank or not
     * a valid date.
     */
    @Nullable
    public static Long parseToEpochDay(@Nullable String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DISPLAY_FORMAT).toEpochDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isBlank(@Nullable String text) {
        return text == null || text.trim().isEmpty();
    }

    public static String formatEpochDay(@Nullable Long epochDay) {
        if (epochDay == null) {
            return "";
        }
        return LocalDate.ofEpochDay(epochDay).format(DISPLAY_FORMAT);
    }

    public static long todayEpochDay() {
        return LocalDate.now().toEpochDay();
    }
}

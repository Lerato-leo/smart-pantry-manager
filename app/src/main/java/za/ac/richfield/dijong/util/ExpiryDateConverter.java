package za.ac.richfield.dijong.util;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Converts between the epoch-day {@code Long} used to store expiry dates and the
 * DD/MM/YYYY string shown in the UI.
 */
public final class ExpiryDateConverter {

    /** Items expiring within this many days are flagged "soon" in the UI and in alerts. */
    public static final int EXPIRY_SOON_WINDOW_DAYS = 3;

    // "uuuu" with STRICT resolving rejects impossible dates like 30/02/2026 instead of
    // quietly rolling them over to 2 March.
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private ExpiryDateConverter() {
    }

    /**
     * @return the epoch day for a DD/MM/YYYY string, or null if the input is blank or not
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

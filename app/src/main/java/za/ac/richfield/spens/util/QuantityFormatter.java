package za.ac.richfield.spens.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats ingredient quantities for display: whole numbers without a trailing ".0"
 * ("800 g", not "800.0 g") and fractions to at most two decimal places ("0.25 kg").
 */
public final class QuantityFormatter {

    // Locale.ROOT keeps "." as the decimal separator, matching what the quantity field
    // accepts, so an edited value round-trips through the form unchanged.
    private static final DecimalFormat QUANTITY_FORMAT =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private QuantityFormatter() {
    }

    public static String format(double quantity) {
        synchronized (QUANTITY_FORMAT) {
            return QUANTITY_FORMAT.format(quantity);
        }
    }

    /** "800 g", or just "3" when there's no unit. */
    public static String formatWithUnit(double quantity, String unit) {
        String amount = format(quantity);
        return unit == null || unit.trim().isEmpty() ? amount : amount + " " + unit;
    }
}

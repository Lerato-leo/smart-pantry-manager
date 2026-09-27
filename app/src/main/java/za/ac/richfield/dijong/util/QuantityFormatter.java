package za.ac.richfield.dijong.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    /**
     * Counted units that read as words, with their plurals. Measures like "g" or "tbsp" are
     * abbreviations and stay as they are ("800 g", never "800 gs").
     */
    private static final Map<String, String> COUNT_UNIT_PLURALS = new HashMap<>();

    /**
     * Litres are written with a capital L, the way they're printed on packaging, so they
     * can't be mistaken for the number 1. Items saved before this still say "l".
     */
    public static String displayUnit(String unit) {
        return "l".equals(unit) ? "L" : unit;
    }

    static {
        COUNT_UNIT_PLURALS.put("unit", "units");
        COUNT_UNIT_PLURALS.put("slice", "slices");
        COUNT_UNIT_PLURALS.put("clove", "cloves");
        COUNT_UNIT_PLURALS.put("can", "cans");
        COUNT_UNIT_PLURALS.put("bottle", "bottles");
        COUNT_UNIT_PLURALS.put("pinch", "pinches");
        COUNT_UNIT_PLURALS.put("cup", "cups");
    }

    /**
     * A recipe amount: like {@link #formatWithUnit}, but plain counts drop the word "unit",
     * so an ingredient list reads "Onion 1" and "Egg 2" rather than "Onion 1 unit".
     */
    public static String formatRecipeAmount(double quantity, String unit) {
        return "unit".equals(unit) ? format(quantity) : formatWithUnit(quantity, unit);
    }

    /** "800 g", "3 units", "1 can", or just "3" when there's no unit. */
    public static String formatWithUnit(double quantity, String unit) {
        String amount = format(quantity);
        if (unit == null || unit.trim().isEmpty()) {
            return amount;
        }
        unit = displayUnit(unit);
        String plural = COUNT_UNIT_PLURALS.get(unit);
        boolean isOne = amount.equals("1");
        return amount + " " + (plural != null && !isOne ? plural : unit);
    }
}

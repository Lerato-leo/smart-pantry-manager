package za.ac.richfield.dijong.util;

/**
 * Metric or imperial, as chosen in Settings. The app works in one system at a time: the unit
 * menu only offers that system's weights and volumes, and recipe amounts (which are written
 * in metric) are converted for display when imperial is chosen. Spoons, cups and counted
 * units ("can", "slice") are the same in both and are never converted.
 */
public final class UnitSystem {

    private static final double GRAMS_PER_OUNCE = 28.35;
    private static final double OUNCES_PER_POUND = 16;
    private static final double MILLILITRES_PER_FLUID_OUNCE = 29.57;

    private UnitSystem() {
    }

    /** An amount paired with the unit it's expressed in. */
    public static final class Amount {
        public final double quantity;
        public final String unit;

        Amount(double quantity, String unit) {
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    /**
     * Re-expresses a recipe amount for display. In metric, amounts under a kilogram or litre
     * are shown in grams or millilitres ("200 g", not "0.2 kg"); otherwise they stay as they
     * are. With imperial chosen, grams become ounces (or pounds from 1 lb up) and millilitres
     * or litres become fluid ounces, rounded to one decimal place.
     */
    public static Amount forDisplay(double quantity, String unit, boolean imperial) {
        if (unit == null) {
            return new Amount(quantity, null);
        }
        if (!imperial) {
            String canonical = RecipeMatcher.normalizeUnit(unit);
            if (quantity < 1 && "kilogram".equals(canonical)) {
                return new Amount(Math.round(quantity * 1000), "g");
            }
            if (quantity < 1 && "liter".equals(canonical)) {
                return new Amount(Math.round(quantity * 1000), "ml");
            }
            return new Amount(quantity, unit);
        }
        String canonical = RecipeMatcher.normalizeUnit(unit);
        if ("gram".equals(RecipeMatcher.baseUnit(unit)) && !"ounce".equals(canonical) && !"pound".equals(canonical)) {
            double ounces = RecipeMatcher.toBaseQuantity(quantity, unit) / GRAMS_PER_OUNCE;
            if (ounces >= OUNCES_PER_POUND) {
                return new Amount(roundToTenth(ounces / OUNCES_PER_POUND), "lb");
            }
            return new Amount(roundToTenth(ounces), "oz");
        }
        if ("ml".equals(canonical) || "liter".equals(canonical)) {
            double fluidOunces = RecipeMatcher.toBaseQuantity(quantity, unit) / MILLILITRES_PER_FLUID_OUNCE;
            return new Amount(roundToTenth(fluidOunces), "fl oz");
        }
        return new Amount(quantity, unit);
    }

    private static double roundToTenth(double value) {
        return Math.round(value * 10) / 10.0;
    }
}

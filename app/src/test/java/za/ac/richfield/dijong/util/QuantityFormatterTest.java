package za.ac.richfield.dijong.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QuantityFormatterTest {

    @Test
    public void format_dropsTrailingZeroForWholeNumbers() {
        assertEquals("800", QuantityFormatter.format(800.0));
    }

    @Test
    public void format_keepsUpToTwoDecimals() {
        assertEquals("0.25", QuantityFormatter.format(0.25));
        assertEquals("1.5", QuantityFormatter.format(1.5));
        assertEquals("0.33", QuantityFormatter.format(1.0 / 3));
    }

    @Test
    public void formatWithUnit_appendsUnitWhenPresent() {
        assertEquals("800 g", QuantityFormatter.formatWithUnit(800, "g"));
        assertEquals("3", QuantityFormatter.formatWithUnit(3, null));
        assertEquals("3", QuantityFormatter.formatWithUnit(3, " "));
    }

    @Test
    public void formatWithUnit_pluralisesCountedUnits() {
        assertEquals("3 units", QuantityFormatter.formatWithUnit(3, "unit"));
        assertEquals("1 unit", QuantityFormatter.formatWithUnit(1, "unit"));
        assertEquals("2 cans", QuantityFormatter.formatWithUnit(2, "can"));
        assertEquals("0.5 cups", QuantityFormatter.formatWithUnit(0.5, "cup"));
    }

    @Test
    public void formatWithUnit_leavesAbbreviationsAlone() {
        assertEquals("800 g", QuantityFormatter.formatWithUnit(800, "g"));
        assertEquals("2 tbsp", QuantityFormatter.formatWithUnit(2, "tbsp"));
    }

    @Test
    public void formatWithUnit_writesLitresWithCapitalL() {
        assertEquals("1 L", QuantityFormatter.formatWithUnit(1, "L"));
        assertEquals("2 L", QuantityFormatter.formatWithUnit(2, "l"));
        assertEquals("250 ml", QuantityFormatter.formatWithUnit(250, "ml"));
    }
}

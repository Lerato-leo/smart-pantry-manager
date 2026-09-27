package za.ac.richfield.dijong.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

public class ExpiryDateConverterTest {

    @Test
    public void parseToEpochDay_parsesDayMonthYear() {
        long expected = LocalDate.of(2026, 9, 30).toEpochDay();
        assertEquals(Long.valueOf(expected), ExpiryDateConverter.parseToEpochDay("30/09/2026"));
    }

    @Test
    public void parseToEpochDay_trimsSurroundingWhitespace() {
        long expected = LocalDate.of(2026, 1, 5).toEpochDay();
        assertEquals(Long.valueOf(expected), ExpiryDateConverter.parseToEpochDay("  05/01/2026 "));
    }

    @Test
    public void parseToEpochDay_blankOrNullIsNull() {
        assertNull(ExpiryDateConverter.parseToEpochDay(null));
        assertNull(ExpiryDateConverter.parseToEpochDay(""));
        assertNull(ExpiryDateConverter.parseToEpochDay("   "));
    }

    @Test
    public void parseToEpochDay_invalidDatesAreNull() {
        assertNull(ExpiryDateConverter.parseToEpochDay("2026-09-30"));
        assertNull(ExpiryDateConverter.parseToEpochDay("30/02/2026"));
        assertNull(ExpiryDateConverter.parseToEpochDay("31/09/2026"));
        assertNull(ExpiryDateConverter.parseToEpochDay("tomorrow"));
    }

    @Test
    public void formatEpochDay_roundTripsWithParse() {
        String text = "25/12/2026";
        assertEquals(text, ExpiryDateConverter.formatEpochDay(ExpiryDateConverter.parseToEpochDay(text)));
    }

    @Test
    public void formatEpochDay_nullIsEmpty() {
        assertEquals("", ExpiryDateConverter.formatEpochDay(null));
    }

    @Test
    public void isBlank_detectsEmptyInput() {
        assertTrue(ExpiryDateConverter.isBlank(null));
        assertTrue(ExpiryDateConverter.isBlank(" "));
        assertFalse(ExpiryDateConverter.isBlank("30/09/2026"));
    }
}

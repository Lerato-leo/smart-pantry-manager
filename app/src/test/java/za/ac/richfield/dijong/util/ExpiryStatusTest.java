package za.ac.richfield.dijong.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpiryStatusTest {

    private static final long TODAY = 20_000L;

    @Test
    public void noDate_isNone() {
        ExpiryStatus status = ExpiryStatus.of(null, TODAY);
        assertEquals(ExpiryStatus.Kind.NONE, status.getKind());
        assertFalse(status.needsAttention());
    }

    @Test
    public void yesterday_isExpired() {
        ExpiryStatus status = ExpiryStatus.of(TODAY - 1, TODAY);
        assertEquals(ExpiryStatus.Kind.EXPIRED, status.getKind());
        assertEquals(-1, status.getDaysLeft());
        assertTrue(status.needsAttention());
    }

    @Test
    public void todayToThreeDays_isSoon() {
        for (long days = 0; days <= ExpiryDateConverter.EXPIRY_SOON_WINDOW_DAYS; days++) {
            ExpiryStatus status = ExpiryStatus.of(TODAY + days, TODAY);
            assertEquals(ExpiryStatus.Kind.SOON, status.getKind());
            assertEquals(days, status.getDaysLeft());
        }
    }

    @Test
    public void beyondWindow_isFresh() {
        ExpiryStatus status = ExpiryStatus.of(TODAY + ExpiryDateConverter.EXPIRY_SOON_WINDOW_DAYS + 1, TODAY);
        assertEquals(ExpiryStatus.Kind.FRESH, status.getKind());
        assertFalse(status.needsAttention());
    }
}

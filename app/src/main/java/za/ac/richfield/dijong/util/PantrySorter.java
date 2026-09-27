package za.ac.richfield.dijong.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import za.ac.richfield.dijong.data.entity.PantryItem;

/**
 * Orders the pantry list the way the Dijong design shows it: anything expired or expiring
 * soon first, soonest date at the top, then everything else alphabetically.
 */
public final class PantrySorter {

    private PantrySorter() {
    }

    public static List<PantryItem> sortForDisplay(List<PantryItem> items, long todayEpochDay) {
        List<PantryItem> sorted = new ArrayList<>(items);
        sorted.sort(Comparator
                .comparing((PantryItem item) -> !ExpiryStatus.of(item.getExpiryDate(), todayEpochDay).needsAttention())
                .thenComparing(item -> ExpiryStatus.of(item.getExpiryDate(), todayEpochDay).needsAttention()
                        ? item.getExpiryDate() : Long.MAX_VALUE)
                .thenComparing(item -> item.getName().toLowerCase()));
        return sorted;
    }

    /** How many items are expiring soon (today up to the soon window), not counting expired ones. */
    public static int countExpiringSoon(List<PantryItem> items, long todayEpochDay) {
        int count = 0;
        for (PantryItem item : items) {
            if (ExpiryStatus.of(item.getExpiryDate(), todayEpochDay).getKind() == ExpiryStatus.Kind.SOON) {
                count++;
            }
        }
        return count;
    }
}

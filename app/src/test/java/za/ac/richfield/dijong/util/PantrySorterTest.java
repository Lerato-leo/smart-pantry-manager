package za.ac.richfield.dijong.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import za.ac.richfield.dijong.data.entity.PantryItem;

public class PantrySorterTest {

    private static final long TODAY = 20_000L;

    @Test
    public void sortForDisplay_putsExpiringFirstThenAlphabetical() {
        List<PantryItem> items = Arrays.asList(
                item("Rice", null),
                item("Cream", TODAY + 2),
                item("Butternut", TODAY + 30),
                item("Chicken thighs", TODAY + 1),
                item("apples", null));

        List<PantryItem> sorted = PantrySorter.sortForDisplay(items, TODAY);

        assertEquals(Arrays.asList("Chicken thighs", "Cream", "apples", "Butternut", "Rice"), names(sorted));
    }

    @Test
    public void sortForDisplay_expiredComesBeforeExpiringSoon() {
        List<PantryItem> sorted = PantrySorter.sortForDisplay(Arrays.asList(
                item("Milk", TODAY + 1),
                item("Yoghurt", TODAY - 2)), TODAY);

        assertEquals(Arrays.asList("Yoghurt", "Milk"), names(sorted));
    }

    @Test
    public void countExpiringSoon_excludesExpiredAndFresh() {
        List<PantryItem> items = Arrays.asList(
                item("A", TODAY),
                item("B", TODAY + 3),
                item("C", TODAY - 1),
                item("D", TODAY + 4),
                item("E", null));

        assertEquals(2, PantrySorter.countExpiringSoon(items, TODAY));
    }

    private static PantryItem item(String name, Long expiry) {
        return new PantryItem(0, name, 1, "unit", expiry);
    }

    private static List<String> names(List<PantryItem> items) {
        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            names[i] = items.get(i).getName();
        }
        return Arrays.asList(names);
    }
}

package za.ac.richfield.dijong.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IngredientCategoryTest {

    @Test
    public void fromKey_readsStoredNames() {
        assertEquals(IngredientCategory.DAIRY, IngredientCategory.fromKey("DAIRY"));
        assertEquals(IngredientCategory.VEGETABLES, IngredientCategory.fromKey("VEGETABLES"));
    }

    @Test
    public void fromKey_fallsBackToOther() {
        assertEquals(IngredientCategory.OTHER, IngredientCategory.fromKey(null));
        assertEquals(IngredientCategory.OTHER, IngredientCategory.fromKey(""));
        assertEquals(IngredientCategory.OTHER, IngredientCategory.fromKey("dairy"));
    }

    @Test
    public void defaultKey_isOther() {
        assertEquals(IngredientCategory.OTHER, IngredientCategory.fromKey(IngredientCategory.DEFAULT_KEY));
    }
}

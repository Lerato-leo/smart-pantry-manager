package za.ac.richfield.dijong.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class RecipeStepsTest {

    @Test
    public void split_separatesNumberedSteps() {
        assertEquals(Arrays.asList("Boil the water.", "Stir in the maize meal.", "Serve."),
                RecipeSteps.split("1. Boil the water. 2. Stir in the maize meal. 3. Serve."));
    }

    @Test
    public void split_keepsNumbersThatAreNotStepNumbers() {
        assertEquals(Arrays.asList("Simmer for 10 minutes.", "Serve with 2 slices of bread."),
                RecipeSteps.split("1. Simmer for 10 minutes. 2. Serve with 2 slices of bread."));
    }

    @Test
    public void split_unnumberedMethodIsOneStep() {
        assertEquals(Collections.singletonList("Mix everything and bake."),
                RecipeSteps.split("Mix everything and bake."));
    }

    @Test
    public void split_nullOrBlankIsEmpty() {
        assertTrue(RecipeSteps.split(null).isEmpty());
        assertTrue(RecipeSteps.split("  ").isEmpty());
    }
}

package za.ac.richfield.dijong.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits a recipe's method, stored as one string like "1. Boil the water. 2. Stir in the
 * maize meal.", into its steps so the detail screen can show them as a numbered list.
 */
public final class RecipeSteps {

    /** A step number such as "2. " at the start or after whitespace; "10 minutes." doesn't match. */
    private static final Pattern STEP_NUMBER = Pattern.compile("(?:^|\\s+)\\d+\\.\\s+");

    private RecipeSteps() {
    }

    public static List<String> split(String method) {
        List<String> steps = new ArrayList<>();
        if (method == null) {
            return steps;
        }
        for (String part : STEP_NUMBER.split(method.trim())) {
            String step = part.trim();
            if (!step.isEmpty()) {
                steps.add(step);
            }
        }
        return steps;
    }
}

package io.github.pureza.happynews.validation;

/**
 * Utility methods for validators
 */
public class Validators {

    /**
     * Private constructor, so this class can't be instantiated
     */
    private Validators() { }


    /**
     * Checks if the text doesn't start or end with dots and doesn't contain
     * consecutive dots
     */
    public static boolean validateDots(String text) {
        // split() documentation: Trailing empty strings are therefore not
        // included in the resulting array.
        if (text.endsWith(".")) {
            return false;
        }

        String[] parts = text.split("\\.");
        for (String part : parts) {
            if (part.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}

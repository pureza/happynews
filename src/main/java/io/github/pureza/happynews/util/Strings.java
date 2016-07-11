package io.github.pureza.happynews.util;

/**
 * String utility methods
 */
public class Strings {

    private Strings() {
        // This class can't be instantiated
    }


    /**
     * Converts a piece of text to sentence case
     */
    public static String toSentenceCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }
}

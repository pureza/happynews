package io.github.pureza.happynews;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utility methods for tests
 */
public class Tests {

    /**
     * Private constructor, so the class can't be initialized
     */
    private Tests() {

    }


    /**
     * Creates a date from the given date/time components
     */
    public static Date date(int year, int month, int day, int hour, int min) {
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, min);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}

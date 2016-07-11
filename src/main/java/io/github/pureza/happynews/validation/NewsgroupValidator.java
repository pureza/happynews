package io.github.pureza.happynews.validation;

import java.util.regex.Pattern;

/**
 * Validator for Newsgroups
 */
public class NewsgroupValidator {

    /** Pattern used to validate the name of a newsgroup */
    public static final Pattern NEWSGROUP_NAME_PATTERN = Pattern.compile("[a-zA-Z_][\\w.]*");


    /**
     * Checks if the name of a newsgroup is valid
     *
     * The name can contain words, digits, dots and underscores. However,
     * it must start with a letter or underscore, can't end with a dot nor
     * have two consecutive dots.
     */
    public boolean isValidNewsgroupName(String name) {
        return NEWSGROUP_NAME_PATTERN.matcher(name).matches() && Validators.validateDots(name);
    }


    /**
     * Checks if a comma separated list of newsgroup names is valid
     */
    public boolean isValidListOfNewsgroupNames(String list) {
        if (list.endsWith(",")) {
            return false;
        }

        String[] names = list.split(",");
        for (String name : names) {
            if (!isValidNewsgroupName(name.trim())) {
                return false;
            }
        }

        return true;
    }
}

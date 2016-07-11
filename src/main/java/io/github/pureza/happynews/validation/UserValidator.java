package io.github.pureza.happynews.validation;

import java.util.regex.Pattern;

/**
 * Validator for Users
 */
public class UserValidator {

    /** Pattern used to validate a username */
    public static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z_][\\w.]*");

    /** Pattern used to validate a password */
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("\\S+");


    /**
     * Checks if the username is valid
     *
     * The username can contain words, digits, dots and underscores. However,
     * it must start with a letter or underscore, can't end with a dot nor
     * have two consecutive dots.
     */
    public boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches() && Validators.validateDots(username);
    }


    /**
     * Checks if the password is valid
     *
     * A password can contain anything except whitespace.
     */
    public boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}

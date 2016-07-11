package io.github.pureza.happynews.server;

/**
 * Exception thrown when the authentication credentials are wrong
 */
public class InvalidLoginException extends Exception {
    public InvalidLoginException() {
        super("Invalid login");
    }
}

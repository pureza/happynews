package io.github.pureza.happynews.server;

/**
 * Exception thrown when the user issues a non-existent command
 */
public class UnknownCommandException extends Exception {
    public UnknownCommandException(String command) {
        super("500 " + command + ": Command not recognized");
    }
}

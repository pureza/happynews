package io.github.pureza.happynews.command;

import io.github.pureza.happynews.config.Config;
import io.github.pureza.happynews.user.User;

import java.io.*;

import io.github.pureza.happynews.server.UnknownCommandException;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.util.Strings;


/**
 * Base class for all commands.
 */
public abstract class Command {
    /** Client socket reader */
    protected final BufferedReader in;

    /** Client socket writer */
    protected final PrintStream out;

    /** The client */
    protected final User client;

    /** The server */
    protected final NNTPServer server;

    /** Command arguments */
    protected String[] args;


    /**
     * Generic constructor
     *
     * @param client The user who called the command
     * @param args Command arguments
     */
    public Command(User client, String args, NNTPServer server) throws IOException {
        in = new BufferedReader(
                new InputStreamReader(
                        new BufferedInputStream(
                                client.getClientSocket().getInputStream())));

        out = new PrintStream(client.getClientSocket().getOutputStream(), true);
        this.client = client;
        this.args = args.split(" ");
        this.server = server;

        // Make sure we instantiated the right command class
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Command called with empty arguments");
        }

        if (!((Strings.toSentenceCase(this.args[0]) + "Command").equals(this.getClass().getSimpleName()))) {
            throw new IllegalArgumentException("Wrong command class instantiated! This is a bug!");
        }
    }


    /**
     * Process the command
     */
    public abstract void process() throws IOException;


    /**
     * Returns the application configuration
     */
    protected Config config() {
        return server.config();
    }


    /**
     * Instantiates the Command instance corresponding to the command requested
     *
     * Uses reflection.
     *
     * @param client The user who called the command
     * @param args Command arguments
     * @return The command instance
     * @throws UnknownCommandException The command does not exist
     */
    @SuppressWarnings("unchecked")
    public static Command parse(User client, String args, NNTPServer server)
            throws UnknownCommandException {
        String cmdName = args.split(" ")[0];
        cmdName = Strings.toSentenceCase(cmdName);
        try {
            Class<Command> clss = (Class<Command>) Class.forName("io.github.pureza.happynews.command." + cmdName + "Command");
            return (Command) clss.getConstructors()[0].newInstance(client, args, server);
        } catch (Exception e) {
            throw new UnknownCommandException(args);
        }
    }
}

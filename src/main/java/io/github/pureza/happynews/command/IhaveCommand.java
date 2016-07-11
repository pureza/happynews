package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

/**
 * IHAVE command
 *
 * Allows the user to inform the server it has a certain article, letting the
 * server decide whether it wants the article or not.
 *
 * This command is not supported.
 *
 * Usage: IHAVE <article-id>
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class IhaveCommand extends Command {

    public IhaveCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        out.println("435 article not wanted - do not send it");
    }
}

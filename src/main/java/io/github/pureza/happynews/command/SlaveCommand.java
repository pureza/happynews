package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import java.io.IOException;

/**
 * SLAVE Command
 *
 * Not implemented.
 */
@SuppressWarnings("unused")
public class SlaveCommand extends Command {
    public SlaveCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        out.println("202 slave status noted");
    }
}

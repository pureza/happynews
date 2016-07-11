package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Admin;

import java.io.IOException;

/**
 * USERRM Command
 *
 * Remove a user.
 * Fails if the user is online.
 *
 * Usage:   USERRM <username>
 * Example: USERRM john
 * Permission: Admin
 */
@SuppressWarnings("unused")
public class UserrmCommand extends Command {

    public UserrmCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }

    public void process() throws IOException {
        if (args.length != 2) {
            out.println("501 command syntax error: USERRM <username>");
            return;
        }

        if (!(client instanceof Admin)) {
            out.println("502 permission denied");
            return;
        }

        if (server.removeUser(args[1])) {
            out.println("282 user removed");
        } else {
            out.println("482 user not removed - perhaps user is online?");
        }
    }
}


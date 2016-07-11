package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Admin;

import java.io.IOException;

/**
 * USERCH Command
 *
 * Updates the role of a user.
 * This command fails if the user is currently online.
 *
 * Usage:   USERCH <username> <new role>
 * Example: USERCH john Admin
 * Permission: Admin
 */
@SuppressWarnings("unused")
public class UserchCommand extends Command {
    public UserchCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }

    @Override
    public void process() throws IOException {
        if (args.length != 3) {
            out.println("501 command syntax error");
            return;
        }

        if (!(client instanceof Admin)) {
            out.println("502 permission denied");
            return;
        }

        try {
            User.Role role = User.Role.valueOf(args[2].toUpperCase());
            if (server.changeUserRole(args[1], role)) {
                out.println("283 role changed");
            } else {
                out.println("483 role unchanged");
            }
        } catch (IllegalArgumentException ex) {
            // Invalid role
            out.println("483 role unchanged");
        }
    }
}

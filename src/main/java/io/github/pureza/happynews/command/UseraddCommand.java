package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.UserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * USERADD Command
 *
 * Adds a new user to the system.
 *
 * Usage:   USERADD <username> <password> <role>
 * Example: USERADD john skcusoturan admin
 *          USERADD doe uramihsoro reader
 *          USERADD james kcorstel editor
 * Permission: Admin
 */
public class UseraddCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public UseraddCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (args.length != 4) {
            out.println("501 command syntax error");
            return;
        }

        if (!(client instanceof Admin)) {
            out.println("502 permission denied");
            return;
        }

        try {
            User.Role role = User.Role.valueOf(args[3].toUpperCase());

            // Creates a new instance of the given Role, using reflection
            User u = UserFactory.createUser(args[1], args[2], role, config());
            if (server.addUser(u)) {
                out.println("281 user added");
            } else {
                out.println("481 user already exists");
            }
        } catch (Exception e) {
            logger.debug("An error occurred while adding a new user", e);

            // Probably, no such role exists...
            out.println("481 operation not performed");
        }
    }
}

package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.util.Strings;

import java.io.IOException;

/**
 * USERLIST Command
 *
 * List all users in the database, including their role and the IP address, if
 * the user is online.
 * With the --onlineonly parameter, lists only the online users.
 *
 * Usage:   USERLIST [--onlineonly]
 * Example: USERLIST
 * Permission: Admin
 */
@SuppressWarnings("unused")
public class UserlistCommand extends Command {

    public UserlistCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (!(client instanceof Admin)) {
            out.println("502 permission denied");
            return;
        }

        out.println("280 User list follows");
        synchronized(server.users()) {
            for (User user : server.users().values()) {
                if (args.length >= 2 && !user.isOnline()) {
                    // Ignore offline users
                    continue;
                }
                out.printf("%s %s%s\n", user.getUsername(), Strings.toSentenceCase(user.getRole().name()),
                        user.isOnline() ? " " + user.getClientSocket().getInetAddress().getHostAddress() : "");
            }
        }
        out.println(".");
    }
}

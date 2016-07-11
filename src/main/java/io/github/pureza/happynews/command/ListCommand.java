package io.github.pureza.happynews.command;

import java.io.IOException;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;

/**
 * LIST Command
 *
 * Lists all existing newsgroups. The output looks like:
 *
 *   <name> <number of the latest article> <number of the first article> <y|n>
 *
 * The <y|n> informs the user if he can publish on that newsgroup.
 *
 * Usage:   LIST
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class ListCommand extends Command {

    public ListCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        out.println("215 list of newsgroups follows");

        String canPost = client instanceof Editor ? "y" : "n";

        // XXX Synchronize, because other client thread may change the list of
        // XXX newsgroups concurrently
        synchronized(server.groups()) {
            server.groups().values()
                    .stream()
                    .forEach(group -> out.printf("%s %d %d %s\n", group.getName(), group.getLastArticleNum(), group.getFirstArticleNum(), canPost));
        }
        out.println(".");
    }
}

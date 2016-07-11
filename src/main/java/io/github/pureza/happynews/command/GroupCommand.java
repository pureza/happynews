package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

/**
 * GROUP Command
 *
 * Allows the user to enter a newsgroup
 * The output looks like this:
 *
 *  211 <number of articles in the newsgroup> <number of the first article>
 *      <number of the last article> <newsgroup name> group selected
 *
 * Usage:   GROUP <newsgroup>
 * Example: GROUP alt.tv.twin-peaks
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class GroupCommand extends Command {

    public GroupCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        try {
            String groupName = args[1];
            Newsgroup group = server.getGroup(groupName);
            if (group != null) {
                client.setCurrentGroup(group);

                synchronized (group.articles()) {
                    out.printf("211 %d %d %d %s group selected\n",
                            group.getLastArticleNum() - group.getFirstArticleNum() + 1,
                            group.getFirstArticleNum(),
                            group.getLastArticleNum(),
                            group.getName());
                }
            } else {
                out.println("411 no such news group");
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            out.println("501 command syntax error");
        }
    }
}

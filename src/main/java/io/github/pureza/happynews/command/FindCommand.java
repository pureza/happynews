package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * FIND Command
 *
 * Searches for the given string within the body of all messages belonging to
 * the given list of newsgroups and prints the ids of those where a match occurs.
 * The string may be a regular expression.
 *
 * Usage:   FIND <newsgroup1,[newsgroup2...]> <regular expression>
 * Example: FIND alt.tv.dune,alt.tv.twin-peaks cooper
 *          FIND comp.lang.ruby (<a.*z@[a-zA-Z].pt>)
 * See also: Command FINDHEADER
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class FindCommand extends Command {

    public FindCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (args.length != 3) {
            out.println("501 command syntax error");
            return;
        }

        String[] newsGroups = args[1].split(",");
        String regex = args[2];

        out.println("232 List of matching articles follows");

        // Saves the matching article ids in a set to avoid duplicates
        Set<String> matches = new HashSet<>();
        for (String s : newsGroups) {
            Newsgroup g = server.getGroup(s);
            if (g == null) {
                continue;
            }

            // XXX Other client threads may update the list of articles while we
            // XXX traverse it
            synchronized (g.articles()) {
                for (String artId : g.articles()) {
                    Article a = server.getArticle(artId);
                    if (a.findInBody(regex)) {
                        matches.add(a.getId());
                    }
                }
            }
        }

        matches.forEach(out::println);
        out.println(".");
    }
}

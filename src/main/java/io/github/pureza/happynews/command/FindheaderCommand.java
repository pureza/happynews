package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.newsgroup.Article;

/**
 * FINDHEADER Command
 *
 * Searches for the given string within the header field of all messages
 * belonging to the given list of newsgroups and prints the ids of those where
 * a match occurs. The string may be a regular expression.
 *
 * Usage:   FINDHEADER <newsgroup1,[newsgroup2...]> <Header field>
 *            <regular expression>
 * Example: FINDHEADER alt.music.woooow Subject JesusOnFire
 *          FINDHEADER comp.lang.ruby from (<a.*z@[a-zA-Z].pt>)
 * See also: Command FIND
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class FindheaderCommand extends Command {

    public FindheaderCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (args.length != 4) {
            out.println("501 command syntax error");
            return;
        }

        String[] newsGroups = args[1].split(",");
        String header = args[2];
        String regex = ".*" + args[3] + ".*";

        out.println("231 List of matching articles follows");

        // Saves the matching article ids in a set to avoid duplicates
        Set<String> matches = new HashSet<>();
        for (String s : newsGroups) {
            Newsgroup group = server.getGroup(s);
            if (group == null) {
                continue;
            }

            // XXX Other client threads may update the list of articles while we
            // XXX traverse it
            synchronized(group.articles()) {
                for (String artId : group.articles()) {
                    Article a = server.getArticle(artId);
                    if (a.findInHeader(header, regex)) {
                        matches.add(a.getId());
                    }
                }
            }
        }

        matches.forEach(out::println);
        out.println(".");
    }
}

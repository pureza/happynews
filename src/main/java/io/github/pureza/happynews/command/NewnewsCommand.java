package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * NEWNEWS Command
 *
 * Lists all articles published on a set of newsgroups after the given date.
 * A date must obey the format YYMMDD HHMMSS.
 *
 * Usage:   NEWNEWS <newsgroup1,[newsgroup2...]> <YYMMDD HHMMSS>
 * Example: NEWNEWS alt.tv.twin-peaks,alt.tv.evangelion 930130 123000
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class NewnewsCommand extends Command {

    public NewnewsCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (args.length != 4) {
            out.println("501 command syntax error");
            return;
        }

        String date = args[2];
        String hour = args[3];

        DateFormat fmt = new SimpleDateFormat("yyMMdd HHmmss");
        Date d;
        try {
            d = fmt.parse(date + " " + hour);
        } catch (ParseException e) {
            out.println("501 command syntax error");
            return;
        }

        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        c.setTime(d);

        out.println("230 list of new articles by message-id follows");
        Set<String> articles = new HashSet<>();
        for (String s : args[1].split(",")) {
            Newsgroup group = server.getGroup(s);

            // Ignore non-existent newsgroups
            if (group == null) {
                continue;
            }

            synchronized (group.articles()) {
                // The newest articles are at the end of the list
                for (int i = group.articles().size() - 1; i >= 0; i--) {
                    Article a = server.getArticle(group.articles().get(i));
                    if (a.getDatePosted().after(c.getTime())) {
                        articles.add(a.getId());
                    }
                }
            }
        }
        articles.forEach(out::println);
        out.println(".");
    }
}

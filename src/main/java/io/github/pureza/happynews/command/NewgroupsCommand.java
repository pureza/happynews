package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * NEWGROUPS command
 *
 * Lists all newsgroups created after the given date. The date must obey the
 * following format: YYMMDD HHMMSS
 *
 * The output of this command resembles the output of the LIST command.
 *
 * Usage:   NEWGROUPS <YYMMDD HHMMSS>
 * Example: NEWGROUPS 930130 123000
 * Permission: Reader
 */
@SuppressWarnings("ALL")
public class NewgroupsCommand extends Command {

    public NewgroupsCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (args.length != 3) {
            out.println("501 command syntax error");
            return;
        }

        String date = args[1];
        String hour = args[2];

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
        String canPost = client instanceof Editor ? "y" : "n";

        out.println("231 list of new newsgroups follows");

        // The list of groups can be modified by other threads, so it needs to
        // be synchronized
        synchronized(server.groups()) {
            server.groups().values()
                    .stream()
                    .filter(group -> group.getDateCreated().after(c.getTime()))
                    .forEach(group -> out.printf("%s %d %d %s\n", group.getName(), group.getLastArticleNum(),
                            group.getFirstArticleNum(), canPost));
        }
        out.println(".");
    }
}


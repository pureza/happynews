package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * LS Command
 *
 * Lists the contents of a given directory.
 *
 * When called with no arguments, lists the contents of the current working
 * directory. With the '-d' flag, displays the article subject as well as the
 * newsgroups where the article was published.
 *
 * Usage:   LS [-d] [directory]
 * Example: LS ../../cinema
 *          LS twin-peaks
 *          LS -d evangelion
 * Permission: Editor
 */
@SuppressWarnings("unused")
public class LsCommand extends Command {

    public LsCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        // Check if the user has permission to use this command
        if (!(client instanceof Editor)) {
            out.println("502 permission denied");
            return;
        }

        Editor editor = (Editor) client;
        String pathString;

        // Detailed listing?
        boolean detailed = args.length > 2 || (args.length > 1 && args[1].startsWith("-"));
        if (args.length == 3) {
            pathString = args[2];
        } else if (args.length == 2) {
            pathString = detailed ? "" : args[1];
        } else {
            pathString = "";
        }

        Path path = editor.getPath().resolve(pathString).normalize();

        // Do not go above /users
        Path usersHome = config().usersHome();
        if (!path.startsWith(usersHome)) {
            path = usersHome;
        }

        if (Files.exists(path)) {
            out.println("286 list follows");

            Files.list(path).forEach(article -> {
                if (Files.isDirectory(article)) {
                    out.println(article.getFileName().toString() + File.separator);
                } else {
                    StringBuilder output = new StringBuilder("<").append(article.getFileName()).append(">");
                    if (detailed) {
                        // Displays the subject and the list of newsgroups
                        Article a = server.getArticle(output.toString());
                        ArticleHeader header = a.getHeader();
                        if (header.contains("NewsGroups")) {
                            output.append(" ").append(header.get("NewsGroups"));
                        }
                        if (header.contains("Subject")) {
                            output.append(" ").append(header.get("Subject"));
                        }
                    }
                    out.println(output);
                }
            });

            out.println(".");
        } else {
            // Directory doesn't exist
            out.println("486 path doesn't exist");
        }
    }
}


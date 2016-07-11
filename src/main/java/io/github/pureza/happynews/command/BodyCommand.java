package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.validation.ArticleValidator;

import java.io.IOException;

/**
 * BODY command
 *
 * Displays the body of an article.
 *
 * This command may be called in three different ways:
 * - With no arguments, it prints the body of the article referenced by a pointer
 *   within the newsgroup the user is reading
 * - With a numeric argument, displays the body of the article with that number
 *   and updates the pointer to reference to it
 * - With an article id (a string of the form <unique@host>), displays the body
 *   of the article with said id. This option allows the user to read an article
 *   without subscribing to the newsgroup, because unlike article numbers, which
 *   are internal to the newsgroup, article ids are global.
 *
 * Usage:   BODY [article-id | article-number]
 * Example: BODY <876@sapo.pt>
 *          BODY 7842
 * See also: Command ARTICLE, HEAD, STAT, NEXT, LAST
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class BodyCommand extends Command {

    /** Validator for article ids */
    private ArticleValidator articleValidator = new ArticleValidator();


    public BodyCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        int articleIndex = client.getCurrentArticleIndex();

        // Is the argument an article id?
        boolean isArticleId = false;
        if (args.length == 2) {
            try {
                articleIndex = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                // The articleIndex presented in the output should be 0 in this case
                articleIndex = 0;
                isArticleId = true;
            }
        }

        // Check for invalid article ids
        if (args.length > 2 || (isArticleId && !articleValidator.isValidArticleId(args[1]))) {
            out.println("501 command syntax error");
            return;
        }


        // To read the message with the given number, the user must subscribe
        // the newsgroup
        if (!isArticleId && client.getCurrentGroup() == null) {
            out.println("412 no newsgroup has been selected");
            return;
        }

        Article a;
        switch (args.length) {
            case 1:
                // Displays the current message
                if (client.getCurrentGroup().isEmpty()) {
                    out.println("420 no current article has been selected");
                    return;
                }
                a = server.getArticle(client.getCurrentArticleId());
                break;
            case 2:
                if (isArticleId) {
                    String articleID = args[1];
                    a = server.getArticle(articleID);
                    if (a == null) {
                        out.println("430 no such article found");
                        return;
                    }
                } else {
                    // Throws an exception if the first argument is an id
                    if (!client.getCurrentGroup().containsArticleNum(articleIndex)) {
                        out.println("423 no such article number in this group");
                        return;
                    }

                    // Updates the pointer
                    client.setCurrentArticleIndex(articleIndex);
                    a = server.getArticle(client.getCurrentArticleId());
                }

                break;
            default:
                throw new IllegalArgumentException("Can't happen!");
        }

        out.printf("222 %d %s article retrieved - body follows\n", articleIndex, a.getId());
        String body = a.getBody();
        if (body.length() > 0)
            out.println(a.getBody());
        out.println(".");
    }
}



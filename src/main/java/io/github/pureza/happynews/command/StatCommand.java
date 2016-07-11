package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.validation.ArticleValidator;

import java.io.IOException;

/**
 * STAT Command
 *
 * Updates the pointer from the current article to the article whose number is
 * given by the first argument.
 * However, if the argument is an article id, the pointer is not updated, even
 * if the corresponding article lives within the current newsgroup.
 * When called with no arguments, the STAT command displays the number and id
 * of the current article.
 *
 * Usage:   STAT [article-id | article-number]
 * Example: STAT 7831
 * See also: Command ARTICLE, HEAD, BODY, NEXT, LAST
 * Permission: Reader
 */
public class StatCommand extends Command {

    /** Validator for article ids */
    private ArticleValidator articleValidator = new ArticleValidator();


    public StatCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }

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

        out.printf("223 %d %s article retrieved - request text separately\n", articleIndex, a.getId());
    }
}

package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

/**
 * LAST Command
 *
 * Moves the pointer to the previous article within the newsgroup.
 *
 * This command does not print the contents of the article. For that, the user
 * should use the ARTICLE, HEAD or BODY commands.
 *
 * Usage:   LAST
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class LastCommand extends Command {

    public LastCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        Newsgroup currentGroup = client.getCurrentGroup();
        if (currentGroup == null) {
            out.println("412 no newsgroup has been selected");
            return;
        }

        synchronized (currentGroup) {
            if (currentGroup.isEmpty()) {
                out.println("420 no current article has been selected");
                return;
            }

            int articleIndex = client.getCurrentArticleIndex();
            if (!currentGroup.hasPrevious(articleIndex)) {
                out.println("422 no previous article in this group");
                return;
            }

            client.setCurrentArticleIndex(currentGroup.previousIndex(articleIndex));
            Article a = server.getArticle(client.getCurrentArticleId());
            out.printf("223 %d %s article retrieved - request text separately\n", client.getCurrentArticleIndex(), a.getId());
        }
    }
}

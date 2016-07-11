package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.validation.ArticleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RM Command
 *
 * Deletes an article from the editor's home.
 * The corresponding newsgroup article is not deleted.
 *
 * Usage:   RM [path/]<message-id>
 * Example: RM <786@yahoo.com>
 *          RM ../<83@netcabo.pt>
 * Permission: Editor
 */
@SuppressWarnings("unused")
public class RmCommand extends Command {

    /** Used to validate article ids */
    private ArticleValidator articleValidator = new ArticleValidator();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RmCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (!(client instanceof Editor)) {
            out.println("502 permission denied");
            return;
        }

        Editor editor = (Editor) client;

        if (args.length != 2) {
            out.println("501 command syntax error");
            return;
        }

        Path argPath = editor.getPath().resolve(Paths.get(args[1])).normalize();
        String articleId = argPath.getFileName().toString();

        // Check if the article id follows the <id@host> format
        if (!(articleValidator.isValidArticleId(articleId))) {
            out.println("501 command syntax error");
            return;
        }

        String fileName = articleId.substring(1, articleId.length() - 1);
        Path filePath = argPath.getParent().resolve(fileName);

        Path userHome = editor.getHome();

        // Can't delete other user's files
        if (filePath.startsWith(userHome)) {
            try {
                Files.delete(filePath);
                out.println("290 article removed from your local directory");
            } catch (IOException ex) {
                logger.debug("Unable to rm {}", argPath, ex);
                out.println("490 no such article");
            }
        } else {
            out.println("502 permission denied");
        }
    }
}



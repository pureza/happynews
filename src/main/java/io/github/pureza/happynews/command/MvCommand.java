package io.github.pureza.happynews.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.validation.ArticleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MV Command
 *
 * Moves an article to a different directory.
 * It is not allowed to move articles outside an editor's home.
 *
 * Usage:   MV <old path> <new path>
 * Example: MV <672@sapo.pt> pink-floyd
 *          MV ../<83@netcabo.pt> .
 * Permission: Editor
 */
@SuppressWarnings("unused")
public class MvCommand extends Command {

    /** Used to validate article ids */
    private ArticleValidator articleValidator = new ArticleValidator();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MvCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        // Check if the user has the right permission
        if (!(client instanceof Editor)) {
            out.println("502 permission denied");
            return;
        }

        // Check if the command is well formed
        if (args.length != 3) {
            out.println("501 command syntax error");
            return;
        }

        Editor editor = (Editor) client;
        Path cwd = editor.getPath();

        Path srcPath = cwd.resolve(args[1]).normalize();
        Path dstPath = cwd.resolve(args[2]).normalize();

        String articleId = srcPath.getFileName().toString();

        // Check if the article id follows the <id@host> format
        if (!(articleValidator.isValidArticleId(articleId))) {
            out.println("501 command syntax error");
            return;
        }

        String fileName = articleId.substring(1, articleId.length() - 1);
        Path srcFilePath = srcPath.getParent().resolve(fileName).normalize();
        Path dstFilePath = dstPath.resolve(fileName);

        // Does the article exist?
        if (!Files.exists(srcFilePath)) {
            out.println("488 article not found");
            return;
        }

        // Does the new path exist?
        if (!Files.exists(dstPath)) {
            out.println("488 path not found");
            return;
        }


        Path home = editor.getHome();

        // Are we moving an article within our home?
        if (dstFilePath.startsWith(home) && (srcPath.startsWith(home))) {
            try {
                Files.move(srcFilePath, dstFilePath);
                out.println("288 article moved");
            } catch (IOException ex) {
                logger.debug("Unable to mv {}", srcFilePath, dstFilePath);
                out.println("488 move failed");
            }
        } else {
            out.println("502 Permission denied");
        }
    }
}


package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * RMDIR Command
 *
 * Removes a directory inside an editor's home. The directory must be empty.
 *
 * Usage:   RMDIR <path>
 * Example: RMDIR informatics
 *          RMDIR old/stuff
 * Permission: Editor
 */
@SuppressWarnings("unused")
public class RmdirCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RmdirCommand(User client, String args, NNTPServer server) throws IOException {
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

        String arg = args[1];
        Path path = editor.getPath().resolve(arg).normalize();

        // Unable to delete the current working directory
        if (path.equals(editor.getPath())) {
            out.println("491 can't delete current directory");
            return;
        }

        // Can't touch other user homes
        if (path.startsWith(editor.getHome())) {
            if (Files.isDirectory(path)) {
                try {
                    Files.delete(path);
                    out.println("291 directory removed");
                } catch (Exception ex) {
                    logger.debug("Unable to rmdir {}", path, ex);

                    out.println("491 directory not removed");
                }
            } else {
                out.println("491 not a directory");
            }
        } else {
            out.println("502 permission denied");
        }
    }
}

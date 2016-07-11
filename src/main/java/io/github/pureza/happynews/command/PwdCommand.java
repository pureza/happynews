package io.github.pureza.happynews.command;

import java.io.IOException;
import java.nio.file.Path;

import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.server.NNTPServer;

/**
 * PWD Command
 *
 * Displays the current working directory.
 *
 * Usage:   PWD
 * Permission: Editor
 */
@SuppressWarnings("unused")
public class PwdCommand extends Command {

    public PwdCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (!(client instanceof Editor)) {
            out.println("502 permission denied");
            return;
        }

        Editor editor = (Editor) client;
        Path path = editor.getPath();
        Path relativePath = config().usersHome().relativize(path);

        out.println("289 /" + relativePath.toString());
    }
}

package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;

import java.io.IOException;

/**
 * MODE command
 *
 * Used by news readers, such as Mozilla Thunderbird, to tell the server that
 * they want only to read the news in sequential order. This way, the server
 * can adapt to these clients and serve them in a more efficient manner.
 *
 * Not supported.
 *
 * Usage: MODE reader
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class ModeCommand extends Command {

    public ModeCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (client instanceof Editor) {
            out.println("200 Hello, you can post");
        } else {
            out.println("201 Hello, you can't post");
        }
    }
}

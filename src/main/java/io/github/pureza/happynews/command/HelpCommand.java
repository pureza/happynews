package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

/**
 * HELP Command
 *
 * Prints the list of all the supported commands.
 *
 * Usage: HELP
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class HelpCommand extends Command {

    public HelpCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        String userCommands =
                " - Generic commands:\n" +
                        "\n   --------------------- Navigation --------------------" +
                        "\n   GROUP      Switch to a new newsgroup" +
                        "\n   LIST       List all newsgroups" +
                        "\n   NEWGROUPS  List all newsgroups created after the given date" +
                        "\n   NEWNEWS    List all articles published after the given date" +
                        "\n   ------------------ Reading articles -----------------" +
                        "\n   ARTICLE    Display an article" +
                        "\n   HEAD       Display the header of an article" +
                        "\n   BODY       Display the body of an article" +
                        "\n   STAT       Update the pointer to the current article" +
                        "\n   NEXT       Go to the next article" +
                        "\n   LAST       Go to the previous article" +
                        "\n   XOVER      Display a summary of a set of articles" +
                        "\n   FIND       Look for a string or regular expression within the body of all articles" +
                        "\n   FINDHEADER Look for a string or regular expression within the header of all articles" +
                        "\n   -------------------- Not supported ------------------" +
                        "\n   IHAVE" +
                        "\n   MODE" +
                        "\n   SLAVE" +
                        "\n   ---------------- Account management  ----------------" +
                        "\n   PASSWD     Change password";

        String editorCommands =
                "\n - Editor commands:\n" +
                        "\n   ----------------- Article management ----------------" +
                        "\n   POST       Publish an article" +
                        "\n   ------------ Working directory management -----------" +
                        "\n   CD         Change the current working directory" +
                        "\n   PWD        Print the current working directory" +
                        "\n   LS         Display the content of a directory" +
                        "\n   MKDIR      Create a new directory" +
                        "\n   RMDIR      Remove a directory" +
                        "\n   MV         Move an article" +
                        "\n   RM         Remove an article";

        String adminCommands =
                "\n - Administrator commands:\n" +
                        "\n   ------------------ Group management -----------------" +
                        "\n   NEWGROUP   Create a new newsgroup" +
                        "\n   ------------------ User management ------------------" +
                        "\n   USERLIST   List all users in the system" +
                        "\n   USERADD    Add a user" +
                        "\n   USERCH     Update the user role" +
                        "\n   USERRM     Remove a user";

        out.println(userCommands);
        if (client instanceof Editor) {
            out.println(editorCommands);
        }
        if (client instanceof Admin) {
            out.println(adminCommands);
        }
        out.println(".");
    }
}

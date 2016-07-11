package io.github.pureza.happynews.command;

import java.io.IOException;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * POST Command
 *
 * Publishes an article.
 * The format of the article must follow the USENET standard, as described in
 * RFC 1036. In this format, the header and the body are separated by an empty
 * line. Each header line defines a field with the format:
 *
 *   <Identifier>: <value>
 *
 * For example,
 *
 *   From: pureza@gmail.com
 *   Subject: hello world from HappyNews
 *   Newsgroups: alt.tv.twin-peaks,dei.uc.irc
 *
 * The three fields in the example above are mandatory.
 * After the header (and the empty line), the user can start typing the
 * article. To complete the command and publish the article, the user should
 * end the input with a line containing a single dot '.'.
 *
 * Usage:   POST
 * Permission: Reader
 */
@SuppressWarnings("unused")
public class PostCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PostCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (!(client instanceof Editor)) {
            out.println("440 posting not allowed");
            return;
        }

        out.println("340 send article to be posted. End with <CR-LF>.<CR-LF>");

        String line;
        StringBuilder headerText = new StringBuilder();

        // Read the header
        while ((line = in.readLine()) != null && line.length() > 0) {
            logger.info("{}: {}", client.getUsername(), line);

            if (line.equals(".")) {
                if (headerText.length() == 0) {
                    out.println("441 posting failed: blank message?");
                    return;
                }
                break;
            }

            headerText.append(line);
            headerText.append("\n");
        }

        // Delete the last '\n'
        headerText.deleteCharAt(headerText.length() - 1);

        StringBuilder body = new StringBuilder();

        // Read the body
        if (line != null && !line.equals(".")) {
            while ((line = in.readLine()) != null && !line.equals(".")) {
                body.append(line);
                body.append("\n");
            }
            if (body.length() > 0) {
                // Delete the last '\n'
                body.deleteCharAt(body.length() - 1);
            }
        }

        ArticleHeader header = new ArticleHeader(headerText.toString());

        // NNTPServer.postArticle fails if any of the mandatory fields are missing
        if (server.postArticle(header, body.toString(), (Editor) client)) {
            out.println("240 article posted ok");
        } else {
            out.println("441 posting failed: missing headers?");
        }
    }
}

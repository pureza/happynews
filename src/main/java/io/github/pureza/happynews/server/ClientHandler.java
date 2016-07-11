package io.github.pureza.happynews.server;

import io.github.pureza.happynews.user.User;

import java.net.Socket;
import java.io.*;

import io.github.pureza.happynews.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client thread
 */
public class ClientHandler extends Thread {

    /** The client socket */
    private final Socket clientSock;

    /** The user corresponding to this handler */
    private User client;

    /** The server */
    private final NNTPServer server;

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public ClientHandler(Socket clientSock, NNTPServer server) {
        this.clientSock = clientSock;
        this.server = server;
    }


    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(clientSock.getInputStream())));
             PrintStream out = new PrintStream(clientSock.getOutputStream(), true)) {

            // Authenticate the user
            authenticate(in, out);

            client.setClientSocket(clientSock);
            logger.info("{} authenticated himself as {}", clientSock.getInetAddress().getHostAddress(), client.getUsername());

            // Main loop: read and processes commands, until the user quits
            while (true) {
                String s = in.readLine();

                if (s.toLowerCase().startsWith("quit")) {
                    break;
                }

                if (s.equals("")) {
                    continue;
                }

                try {
                    Command cmd = Command.parse(client, s, server);
                    logger.info("{}: {}", client.getUsername(), s);
                    cmd.process();
                } catch (UnknownCommandException ex) {
                    out.println("500 " + s + ": Command not recognized");
                    logger.debug("Unknown command: {}", s);
                }
            }
        } catch (Exception ex) {
            logger.error("An error occurred while handling a client", ex);
        } finally {
            try {
                String name = client != null ? client.getUsername() : clientSock.getInetAddress().getHostAddress();
                logger.info("{} left", name);
                clientSock.close();
            } catch (Exception ex) {
                logger.debug("An error occurred", ex);
            }
        }
    }


    /**
     * Authenticates the user
     *
     * @throws InvalidLoginException if the authentication credentials are wrong
     * @param in
     * @param out
     */
    void authenticate(BufferedReader in, PrintStream out) throws IOException, InvalidLoginException {
        String authInfoUser;
        do {
            out.println("480 server ready - authentication required");
            authInfoUser = in.readLine();
        } while (!authInfoUser.toUpperCase().startsWith("AUTHINFO USER"));
        String userName = authInfoUser.split(" ")[2];

        out.println("381 password please...");
        String authInfoPass = in.readLine();
        String password = authInfoPass.split(" ")[2];

        client = server.login(userName, password);
        if (client == null) {
            out.println("482 Invalid login");
            throw new InvalidLoginException();
        }

        out.println("281 Authentication accepted");
    }
}

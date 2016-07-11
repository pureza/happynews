package io.github.pureza.happynews;

import io.github.pureza.happynews.config.Config;
import io.github.pureza.happynews.config.ConfigLoader;
import io.github.pureza.happynews.server.NNTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The main application
 */
public class App {

    /** Default port where the server listens for client connections */
    public static final int DEFAULT_PORT = 1119;


    /** The logger */
    private static final Logger logger = LoggerFactory.getLogger(App.class);


    /**
     * This is where it all starts
     */
    public static void main(String[] args) {
        // Load the configuration
        Config config = null;
        try {
            config = ConfigLoader.load();
        } catch (Exception ex) {
            logger.error("An error occurred while loading the configuration", ex);
            System.exit(1);
        }

        int port = DEFAULT_PORT;
        if (args.length > 1 && args[0].equals("-p")) {
            try {
                port = Integer.parseInt(args[1]);
                if (port < 0 || port >= 65536) {
                    logger.error("The port must be between 0 and 65,536");
                    System.exit(1);
                }
            } catch (NumberFormatException ex) {
                logger.error("Invalid port {}", args[1], ex);
            }
        }

        try {
            (new NNTPServer(port, config)).start();
        } catch (Exception ex) {
            logger.error("An error occurred while initializing the server", ex);
        }
    }
}

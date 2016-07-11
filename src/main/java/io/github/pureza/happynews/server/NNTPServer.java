package io.github.pureza.happynews.server;

import io.github.pureza.happynews.config.Config;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.storage.ArticleRepository;
import io.github.pureza.happynews.storage.GroupRepository;
import io.github.pureza.happynews.storage.UserRepository;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The server
 */
public class NNTPServer extends Thread {

    /** User repository */
    private final UserRepository userRepository;

    /** Group repository */
    private final GroupRepository groupRepository;

    /** Article repository */
    private final ArticleRepository articleRepository;

    /** The server's socket */
    private ServerSocket server;

    /** Application configuration */
    private Config config;

    /** The logger */
    private Logger logger = LoggerFactory.getLogger(NNTPServer.class);


    /**
     * Creates a new server with the given configuration, that will listen for
     * incoming connections at the port specified
     */
    public NNTPServer(int port, Config config) throws Exception {
        this(new ServerSocket(port), config);
    }


    /**
     * Creates a new server with the given socket and configuration
     *
     * Used for testing purposes.
     */
    NNTPServer(ServerSocket socket, Config config) throws Exception {
        this.config = config;
        this.userRepository = new UserRepository(config);
        this.groupRepository = new GroupRepository(config);
        this.articleRepository = new ArticleRepository(config);

        // Creates the server socket
        this.server = socket;

        // Internal thread that stores newsgroups every 10 seconds
        new Timer() {
            {
                schedule(new TimerTask() {
                    public void run() {
                        groupRepository.writeGroups();
                    }
                }, 10000, 10000);
            }
        };
    }


    /**
     * Server main loop
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        logger.info("Server up & listening on {}", server.getLocalPort());
        while (true) {
            try {
                Socket request = server.accept();
                logger.info("New client connection from " + request.getInetAddress().getHostAddress() + ":" + request.getPort());
                (new ClientHandler(request, this)).start();
            } catch (IOException ex) {
                logger.error("An error occurred while waiting for new connections. Retrying...", ex);
            }
        }
    }


    /**
     * Returns the application configuration
     */
    public Config config() {
        return this.config;
    }


    /**
     * Return the list of newsgroups
     *
     * This is a shared resource and must be synchronized!
     */
    public Map<String, Newsgroup> groups() {
        return groupRepository.groups();
    }


    /**
     * Creates a new newsgroup
     *
     * Returns true if the newgroup was added and false if it already exists
     */
    public boolean createGroup(String name) {
        return groupRepository.add(name);
    }


    /**
     * Retrieves the newsgroup with the given name
     */
    public Newsgroup getGroup(String name) {
        return groupRepository.get(name);
    }



    /**
     * Authenticates a user
     *
     * Returns null if the credentials are wrong.
     */
    public User login(String username, String password) {
        if (userRepository.authenticate(username, password)) {
            return userRepository.get(username);
        }

        return null;
    }


    /**
     * Retrieves the given user
     */
    public User getUser(String name) {
        return userRepository.get(name);
    }


    /**
     * Adds a new user
     *
     * Returns true if the user is added and false if it already exists
     */
    public boolean addUser(User user) {
        return userRepository.add(user);
    }


    /**
     * Removes a user
     *
     * Fails to remove if the user doesn't exist or is currently online
     */
    public boolean removeUser(String username) {
        return userRepository.remove(username);
    }


    /**
     * Updates the role of a user
     */
    public boolean changeUserRole(String username, User.Role newRole) {
        return userRepository.changeUserRole(username, newRole);
    }


    /**
     * Updates the user's password
     *
     * Returns false if the user doesn't exist.
     */
    public boolean changeUserPassword(String username, String password) {
        return userRepository.changeUserPassword(username, password);
    }


    /**
     * Return all users, grouped by username
     */
    public Map<String, User> users() {
        return userRepository.users();
    }


    /**
     * Retrieves the article with the given id
     */
    public Article getArticle(String id) {
        return articleRepository.get(id);
    }


    /**
     * Posts a new article
     */
    public boolean postArticle(ArticleHeader header, String body, Editor author) {
        // Set the article's date
        SimpleDateFormat s = new SimpleDateFormat("E, dd MMM yy HH:mm:ss");
        header.put("Date", s.format(new Date()));

        if (articleRepository.add(header, body, author)) {
            // Adds the article to the corresponding newsgroups
            String msgId = header.get("Message-ID");
            String[] tokens = header.get("Newsgroups").split(",");
            for (String name : tokens) {
                synchronized (groupRepository.groups()) {
                    Newsgroup group = groupRepository.get(name.trim());
                    if (group == null) {
                        continue;
                    }

                    group.addArticle(msgId);
                }
            }

            return true;
        } else {
            return false;
        }
    }
}

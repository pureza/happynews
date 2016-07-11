package io.github.pureza.happynews.storage;

import io.github.pureza.happynews.config.Config;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.UserFactory;
import io.github.pureza.happynews.util.Bytes;
import io.github.pureza.happynews.validation.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * User repository
 *
 * Users are saved in a tab separated file.
 *
 * This class is thread safe.
 */
public class UserRepository {

    /** Application configuration */
    private Config config;

    /**
     * Users, grouped by username
     * This is a shared resource and so all accesses must be synchronized.
     */
    private final Map<String, User> users = new HashMap<>();

    /** Used to validate usernames and passwords */
    private UserValidator validator = new UserValidator();

    /** Used to generate salts */
    private SecureRandom secureRandom = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * Creates a new repository and automatically loads the list of users
     * from the file
     */
    public UserRepository(Config config) throws Exception {
        this.config = config;
        this.users.putAll(loadUsers().stream()
                .collect(toMap(User::getUsername, Function.identity())));

        if (this.users.isEmpty()) {
             // Default administration account!
            this.add(new Admin("admin", "admin", config.usersHome().resolve("admin")));
            logger.info("Created default administration account admin/admin");
        }
    }


    /**
     * Creates a new repository with the given users
     *
     * Used for testing purposes.
     */
    UserRepository(Config config, List<User> users) {
        this.config = config;
        this.users.putAll(users.stream()
                .collect(toMap(User::getUsername, Function.identity())));
    }


    /**
     * Returns the user with the given username, of null if none exists
     */
    public User get(String username) {
        synchronized (users) {
            return users.get(username);
        }
    }


    /**
     * Returns all users in the system, grouped by username
     *
     * This is a shared resource and should be synchronized.
     */
    public Map<String, User> users() {
        synchronized (users) {
            return users;
        }
    }


    /**
     * Authenticates a user.
     *
     * Returns false if the credentials are wrong or if the user is online
     */
    public boolean authenticate(String username, String password) {
        User user = get(username);
        if (user == null || user.isOnline()) {
            return false;
        }

        return passwordMatches(password, user);
    }


    /**
     * Adds a new user
     *
     * Returns true if the user is added and false otherwise
     */
    public boolean add(User user) {
        if (!validator.isValidUsername(user.getUsername()) || !validator.isValidPassword(user.getPassword())) {
            return false;
        }

        synchronized (users) {
            if (users.containsKey(user.getUsername())) {
                return false;
            }

            // Hash the password
            hashInitialPassword(user);

            User oldUser = users.put(user.getUsername(), user);
            assert (oldUser == null);

            // Create the user's home if he's an editor
            createEditorHome(user);
        }

        // Save the users
        writeUsers();

        return true;
    }


    /**
     * Removes a user
     *
     * Fails to remove if the user doesn't exist or is currently online
     */
    public boolean remove(String username) {
        synchronized (users) {
            User user = users.get(username);
            if (user == null || user.isOnline()) {
                return false;
            }

            User oldUser = users.remove(username);
            assert (user.equals(oldUser));
        }

        // Save the users
        writeUsers();

        return true;
    }


    /**
     * Updates the role of a user
     *
     * Returns true if the role was changed and false otherwise.
     */
    public boolean changeUserRole(String username, User.Role role) {
        synchronized (users) {
            User user = users.get(username);
            if (user == null || user.isOnline()) {
                return false;
            }

            // Fail if the role is the current one
            if (user.getRole().equals(role)) {
                return false;
            }

            try {
                User newUser = UserFactory.createUser(user.getUsername(), user.getPassword(), role, config);

                // Don't call remove() because we don't want to save the list of users at this point
                User removedUser = users.remove(user.getUsername());
                assert (user.equals(removedUser));

                // Saves the new list to a file
                boolean added = add(newUser);
                assert (added);
            } catch (Exception ex) {
                logger.error("An error occurred while changing the role of user {} to {}", username, role, ex);
                return false;
            }
        }

        return true;
    }


    /**
     * Updates the user's password
     *
     * Returns false if the user doesn't exist or if the new password is invalid.
     */
    public boolean changeUserPassword(String username, String newPassword) {
        if (!validator.isValidPassword(newPassword)) {
            return false;
        }

        synchronized (users) {
            User user = users.get(username);
            if (user == null) {
                return false;
            }

            user.setPassword(newPassword);
            hashInitialPassword(user);
        }

        // Save users
        writeUsers();

        return true;
    }


    /**
     * Reads the list of users from a file
     */
    List<User> loadUsers() throws Exception {
        Path usersHome = config.usersHome();

        // Create the /users folder, if it doesn't exist
        if (!Files.exists(usersHome)) {
            logger.info("Creating the users home folder at {}...", usersHome);

            try {
                Files.createDirectory(usersHome);
            } catch (IOException ex) {
                logger.error("An error occurred while creating the users home folder at {}", usersHome, ex);
                throw ex;
            }
        }

        // If the users.tsv file doesn't exist, don't bother reading it
        if (!Files.exists(config.usersFile())) {
            return Collections.emptyList();
        }

        List<User> users = new ArrayList<>();
        try (BufferedReader in = Files.newBufferedReader(config.usersFile())) {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    String[] parts = line.split("\t");
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String salt = parts[2].trim();
                    User.Role role = User.Role.valueOf(parts[3].trim());
                    User user = UserFactory.createUser(username, password, role, config);
                    user.setSalt(salt);
                    users.add(user);
                    createEditorHome(user);
                } catch (Exception ex) {
                    logger.error("An error occurred while reading the list of users from {}, at line '{}'", config.usersFile(), line, ex);
                    throw ex;
                }
            }

            logger.info("{} users loaded", users.size());
        } catch (IOException ex) {
            logger.error("An error occurred while reading the list of users from {}", config.usersFile(), ex);
            throw ex;
        }

        return users;
    }


    /**
     * Writes the list of users to a file
     */
    void writeUsers() {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(config.usersFile()))) {
            synchronized (users) {
                users.values().forEach(user -> {
                    out.printf("%s\t%s\t%s\t%s\n", user.getUsername(), user.getPassword(), user.getSalt(), user.getRole().name());
                });
            }
        } catch (IOException ex) {
            logger.error("An error occurred while persisting the users", ex);
        }
    }


    /**
     * Creates the user's home if he is an editor
     */
    private void createEditorHome(User user) {
        if (user instanceof Editor) {
            Path path = ((Editor) user).getHome();
            if (!Files.exists(path)) {
                try {
                    Files.createDirectory(path);
                } catch (IOException ex) {
                    logger.error("Unable to create the home of user {}", user.getUsername(), ex);
                }
            }

            assert (Files.exists(path));
        }
    }


    /**
     * Checks if the supplied password matches the user's password
     */
    private boolean passwordMatches(String password, User user) {
        String salt = user.getSalt();
        String hashed = hashPassword(password, Bytes.fromHex(salt));
        return hashed.equals(user.getPassword());
    }


    /**
     * Hashes the user password with a random salt
     */
    private void hashInitialPassword(User user) {
        byte[] salt = genSalt();
        user.setPassword(hashPassword(user.getPassword(), salt));
        user.setSalt(Bytes.toHex(salt));
    }


    /**
     * Hashes the password with the salt
     */
    private String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(password.getBytes());
            return Bytes.toHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            // Won't happen
            throw new RuntimeException("SHA-256 algorithm unavailable?");
        }
    }


    /**
     * Generates a random salt with 32 bytes using SHA-256
     */
    private byte[] genSalt() {
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);
        return salt;
    }
}

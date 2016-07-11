package io.github.pureza.happynews.storage;

import io.github.pureza.happynews.config.Config;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.validation.NewsgroupValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Group repository
 *
 * Groups are saved in a tab separated file.
 *
 * This class is thread safe.
 */
public class GroupRepository {

    /**
     * Newsgroups, grouped by name
     * This is a shared resource and should be synchronized.
     */
    private final Map<String, Newsgroup> groups = new HashMap<>();

    /** Application configuration */
    private final Config config;

    /** Used to validate newsgroup names */
    private final NewsgroupValidator validator = new NewsgroupValidator();

    private final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * Creates a new repository and automatically loads the list of newsgroups
     * from the file
     */
    public GroupRepository(Config config) {
        this.config = config;
        this.groups.putAll(loadGroups().stream()
                .collect(toMap(Newsgroup::getName, Function.identity())));
    }


    /**
     * Creates a new repository with the given newsgroups
     *
     * Used for testing purposes.
     */
    GroupRepository(Config config, List<Newsgroup> groups) {
        this.config = config;
        this.groups.putAll(groups.stream()
                .collect(toMap(Newsgroup::getName, Function.identity())));
    }


    /**
     * Returns all groups in the system, grouped by name
     *
     * This is a shared resource and should be synchronized.
     */
    public Map<String, Newsgroup> groups() {
        synchronized (groups) {
            return groups;
        }
    }


    /**
     * Adds a new newsgroup
     *
     * Returns true if the newsgroup was added
     */
    public boolean add(String name) {
        name = name.toLowerCase();
        if (!validator.isValidNewsgroupName(name)) {
            return false;
        }

        synchronized (groups) {
            if (groups.containsKey(name)) {
                return false;
            }

            groups.put(name, new Newsgroup(name));
            return true;
        }
    }


    /**
     * Retrieves the newsgroup with the given name
     */
    public Newsgroup get(String name) {
        synchronized (groups) {
            return groups.get(name);
        }
    }


    /**
     * Reads the list of users from a file
     */
    List<Newsgroup> loadGroups() {
        List<Newsgroup> groups = new ArrayList<>();

        // If the groups.tsv file does not exist, don't bother reading it
        if (!Files.exists(config.groupsFile())) {
            return groups;
        }

        try (BufferedReader in = Files.newBufferedReader(config.groupsFile())) {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    // Ignore blank lines (including the last one)
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split("\t");
                        String name = parts[0].trim();
                        Date dateCreated = new Date(Long.valueOf(parts[1].trim()));

                        List<String> articles = new ArrayList<>();
                        if (parts.length == 3) {
                            Collections.addAll(articles, parts[2].trim().split(","));
                        }
                        groups.add(new Newsgroup(name, dateCreated, articles));
                    }
                } catch (Exception ex) {
                    logger.error("An error occurred while reading the list of articles from {}, at line '{}'", config.groupsFile(), line, ex);
                    throw new RuntimeException(ex);
                }
            }

            logger.info("{} groups loaded", groups.size());
        } catch (IOException ex) {
            logger.error("An error occurred whlie reading the list of groups from {}", config.groupsFile(), ex);
        }

        return groups;
    }


    /**
     * Writes the list of groups to a file
     */
    public void writeGroups() {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(config.groupsFile()))) {
            synchronized (groups) {
                groups.values().forEach(group -> {
                    out.printf("%s\t%s\t%s\n", group.getName(), group.getDateCreated().getTime(), group.articles().stream().collect(Collectors.joining(",")));
                });
            }
        } catch (IOException ex) {
            logger.error("An error occurred while persisting the groups", ex);
        }
    }
}

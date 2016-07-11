package io.github.pureza.happynews.storage;

import io.github.pureza.happynews.config.Config;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.validation.ArticleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

/**
 * Repository for articles
 *
 * Articles are stored as files inside articles/. The name of the file is the article id
 * (a string of the form <article-number@sender-host>).
 *
 * An empty file with the same name is also created inside the editor's home.
 *
 * This class is thread safe.
 */
public class ArticleRepository {

    /** Used to validate article components */
    private ArticleValidator articleValidator = new ArticleValidator();

    /** Application configuration */
    private Config config;

    /**
     * Articles, grouped by id
     * This is a shared resource and all accesses must be synchronized.
     */
    private final Map<String, Article> articles = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public ArticleRepository(Config config) {
        this.config = config;
        this.articles.putAll(loadArticles());
    }


    /**
     * Returns the article with the given id
     */
    public Article get(String id) {
        synchronized (articles) {
            return articles.get(id);
        }
    }


    /**
     * Adds a new article
     *
     * Returns true if the article was added and false otherwise.
     * If the article was added, it's message-id is set in the header.
     */
    public boolean add(ArticleHeader header, String body, Editor author) {
        if (!articleValidator.isValidHeader(header)) {
            logger.error("Invalid header {}", header);
            return false;
        }

        synchronized (articles) {
            // The article id is <article-number@host>, where the article number is
            // the sequential integer counting the number of articles posted on the
            // server and host is the sender's hostname.
            int unique = articles.size() + 1;
            Matcher matcher = ArticleValidator.FROM_PATTERN.matcher(header.get("From"));

            if (matcher.find()) {
                String host = matcher.group(2);

                // Create a file named 'article-number@host' within articles/
                File articleFile = config.articlesHome().resolve(unique + "@" + host).toFile();

                String msgId = "<" + articleFile.getName() + ">";
                header.put("Message-ID", msgId);

                try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(articleFile)))) {
                    out.println(header);
                    out.println();
                    out.print(body);

                    // Create an empty file named after the article id in the user's home
                    File userFile = new File(author.getHome() + File.separator + articleFile.getName());
                    boolean created = userFile.createNewFile();
                    assert (created);

                    articles.put(msgId, new Article(msgId, config.articlesHome()));
                } catch (IOException e) {
                    logger.error("An error occurred while saving the article", e);
                    return false;
                }

                return true;
            } else {
                logger.error("This should not happen, because if the From is invalid, isValidHeader() should have caught that!");
                return false;
            }
        }
    }


    /**
     * Reads the articles
     */
    Map<String, Article> loadArticles() {
        Map<String, Article> articles = new HashMap<>();

        Path articlesHome = config.articlesHome();
        if (!Files.exists(articlesHome)) {
            logger.debug("Creating the articles home folder at {}...", articlesHome);

            try {
                Files.createDirectory(articlesHome);
            } catch (IOException ex) {
                logger.error("Unable to create the articles home folder", ex);
            }
        }

        try {
            articles.putAll(Files.list(articlesHome)
                    .map(path -> new Article("<" + path.getFileName().toString() + ">", config.articlesHome()))
                    .collect(toMap(Article::getId, Function.identity())));
        } catch (Exception ex) {
            logger.error("An error occurred while loading the articles", ex);
        }

        logger.info("{} articles found", articles.size());
        return articles;
    }
}

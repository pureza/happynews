package io.github.pureza.happynews.newsgroup;

import io.github.pureza.happynews.validation.ArticleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * An article
 *
 * This class is thread safe.
 */
public class Article {

    /** Article id */
    private final String id;

    /** Article file */
    private final File articleFile;

    /** Used to validate article ids */
    private ArticleValidator articleValidator = new ArticleValidator();

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public Article(String id, Path articlesHome) {
        if (id == null) {
            throw new NullPointerException("id");
        }

        if (articlesHome == null) {
            throw new NullPointerException("articlesHome");
        }

        if (!articleValidator.isValidArticleId(id)) {
            throw new IllegalArgumentException("Illegal article id: " + id);
        }

        if (!Files.exists(articlesHome)) {
            throw new IllegalArgumentException(articlesHome.toString());
        }

        this.id = id;
        this.articleFile = articlesHome.resolve(this.id.substring(1, this.id.length() - 1)).toFile();
    }


    /**
     * Return the article id
     */
    public String getId() {
        return id;
    }


    /**
     * Return the date the article was posted
     */
    public Date getDatePosted() {
        return new Date(articleFile.lastModified());
    }


    /**
     * Return the article header
     */
    public ArticleHeader getHeader() {
        try {
            return new ArticleHeader(articleFile);
        } catch (IOException ex) {
            logger.error("An error occurred while reading the header of article {}", this.id, ex);
            return null;
        }
    }


    /**
     * Return the article body
     */
    public String getBody() {
        StringBuilder body = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(articleFile));

            String s;

            // Skip the header
            while ((s = in.readLine()) != null && s.length() > 0);

            // Read the body
            while ((s = in.readLine()) != null) {
                body.append(s).append("\n");
            }

            // Delete the last newline
            if (body.length() > 0) {
                body.deleteCharAt(body.length() - 1);
            }
        } catch (IOException ex) {
            logger.error("An error occurred while reading the article's body", ex);
        }

        return body.toString();
    }


    /**
     * Searches the given regular expression within the body of the article
     */
    public boolean findInBody(String regex) {
        Matcher m = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE).matcher(getBody());
        return m.find();
    }


    /**
     * Searches the given regular expression within the header of the article
     */
    public boolean findInHeader(String field, String regex) {
        ArticleHeader header = getHeader();
        if (!header.contains(field)) {
            return false;
        }

        String content = header.get(field);
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE).matcher(content);
        return matcher.find();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(id, article.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

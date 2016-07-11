package io.github.pureza.happynews.validation;


import io.github.pureza.happynews.newsgroup.ArticleHeader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates articles
 */
public class ArticleValidator {

    /** Pattern used to validate article ids */
    public final static Pattern ARTICLE_ID_PATTERN = Pattern.compile("<(\\d+)@([a-zA-Z_][\\w\\.]*)>");

    /** Pattern used to validate article ids */
    public final static Pattern FROM_PATTERN = Pattern.compile(".*<([a-zA-Z_][\\w\\.]*)@([a-zA-Z_][\\w\\.]*)>");

    /** Used to validate the list of newsgroups */
    private final NewsgroupValidator newsgroupValidator = new NewsgroupValidator();


    /**
     * Checks if the given article id is valid
     */
    public boolean isValidArticleId(String articleId) {
        Matcher matcher = ARTICLE_ID_PATTERN.matcher(articleId);
        if (matcher.matches()) {
            String host = matcher.group(2);
            return Validators.validateDots(host);
        }

        return false;
    }


    /**
     * Checks if the 'From' header field is valid
     *
     * The validation is very simple and will miss some real world addresses!
     * Only letters, digits, dots and underscores are allowed. Furthermore,
     * both the user and the host components must start with letters or
     * underscores, can't end with a dot nor contain two consecutive dots
     */
    public boolean isValidFrom(String from) {
        Matcher matcher = FROM_PATTERN.matcher(from);
        if (matcher.matches()) {
            String user = matcher.group(1);
            String host = matcher.group(2);
            return Validators.validateDots(user) && Validators.validateDots(host);
        }

        return false;
    }


    /**
     * Ensures the header contains the 'Newsgroups' 'Subject' and 'From' header
     * fields. Furthermore, validates the 'From' field.
     */
    public boolean isValidHeader(ArticleHeader header) {
        // 'Newgroups', 'From' and 'Subject' fields are mandatory
        if (!(header.contains("Newsgroups") && header.contains("From") && header.contains("Subject"))) {
            return false;
        }

        return isValidFrom(header.get("From")) && newsgroupValidator.isValidListOfNewsgroupNames(header.get("Newsgroups"));
    }
}

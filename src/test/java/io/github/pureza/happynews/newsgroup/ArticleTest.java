package io.github.pureza.happynews.newsgroup;


import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Editor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArticleTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test(expected=IllegalArgumentException.class)
    public void failsOnInvalidArticleId() {
        new Article("invalid-article-id", Paths.get("."));
    }


    @Test(expected=NullPointerException.class)
    public void failsOnNullArticleId() {
        new Article(null, Paths.get("."));
    }


    @Test(expected=NullPointerException.class)
    public void failsOnNullPath() {
        new Article("<1@host>", null);
    }


    @Test(expected=IllegalArgumentException.class)
    public void failsOnNonexistentPath() {
        new Article("<1@host>", Paths.get("/nonexistent/a/b/c/d"));
    }


    @Test
    public void createsValidArticleOtherwise() {
        Article article = new Article("<1@host>", Paths.get("."));
        assertThat(article.getId(), equalTo("<1@host>"));
    }


    @Test
    public void getBodyReadsArticleBody() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticleWithEmptyBody("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.getBody(), equalTo(""));
    }


    @Test
    public void findInBodyFindsMatchingStrings() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.findInBody("from"), equalTo(true));
    }


    @Test
    public void findInBodyIsCaseSensitive() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.findInBody("fRoM"), equalTo(false));
    }


    @Test
    public void findInBodyAcceptsRegularExpressions() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.findInBody("fr.m"), equalTo(true));
    }


    @Test
    public void findInBodyIgnoresStringsThatDoNotMatch() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.findInBody("this_piece_of_text_does_not_exist_in_the_body"), equalTo(false));
    }


    @Test
    public void findInHeaderIgnoresNonexistentHeaders() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");
        assertThat(article.findInHeader("nonexistent-header", "Hello"), equalTo(false));
    }


    @Test
    public void findInHeaderIgnoresFieldsThatDoNotMatch() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");
        assertThat(article.findInHeader("subject", "this_piece_of_text_does_not_exist_in_the_header"), equalTo(false));
    }


    @Test
    public void findInHeaderFindsMatchingStrings() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.findInHeader("subject", "Hello"), equalTo(true));
    }


    @Test
    public void findInHeaderIsCaseSensitive() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.findInHeader("subject", "hElLo"), equalTo(false));
    }


    @Test
    public void findInHeaderAcceptsRegularExpressions() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        assertThat(article.findInHeader("subject", "Hell."), equalTo(true));
    }
}

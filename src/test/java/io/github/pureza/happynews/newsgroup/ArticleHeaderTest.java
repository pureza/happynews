package io.github.pureza.happynews.newsgroup;


import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Editor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ArticleHeaderTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void createsHeaderFromText() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                "Subject: The subject\n" +
                "Newsgroups: happynews.users");

        assertThat(header.get("from"), equalTo("editor@example.org"));
        assertThat(header.get("subject"), equalTo("The subject"));
        assertThat(header.get("newsgroups"), equalTo("happynews.users"));
    }


    @Test
    public void createsHeaderFromEmptyText() {
        new ArticleHeader("");
    }


    @Test(expected=IllegalArgumentException.class)
    public void failsWhenTextIsNotCorrectlyFormatted() {
        new ArticleHeader("this is not a valid header");
    }


    @Test(expected=IllegalArgumentException.class)
    public void failsWhenHeaderStartsWithSpaces() {
        new ArticleHeader(" Field: this is a continuation of a previous field");
    }


    @Test
    public void headerFromTextTreatsMultilineFieldsCorrectly() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                        "Subject: The subject\n" +
                        " This is the continuation of the subject\n" +
                        " and so is this\n" +
                        "Newsgroups: happynews.users");

        assertThat(header.get("from"), equalTo("editor@example.org"));
        assertThat(header.get("subject"), equalTo("The subject\n This is the continuation of the subject\n and so is this"));
        assertThat(header.get("newsgroups"), equalTo("happynews.users"));
    }


    @Test
    public void headerFromFileReadsFile() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");

        ArticleHeader header = article.getHeader();
        assertThat(header.get("from"), equalTo("editor@example.org"));
        assertThat(header.get("subject"), equalTo("Hello world"));
        assertThat(header.get("newsgroups"), equalTo("happynews.users"));
    }


    @Test(expected=IOException.class)
    public void headerFromFileFailsWhenFileDoesNotExist() throws IOException {
        new ArticleHeader(Paths.get("./this_file_does_not_exist").toFile());
    }


    @Test
    public void getReturnsExistingField() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                        "Subject: The subject\n" +
                        "Newsgroups: happynews.users");

        assertThat(header.get("From"), equalTo("editor@example.org"));
    }


    @Test
    public void getIgnoresNonexistentField() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                        "Subject: The subject\n" +
                        "Newsgroups: happynews.users");

        assertThat(header.get("non_existent"), is(nullValue()));
    }


    @Test
    public void getIsCaseInsensitive() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                        "Subject: The subject\n" +
                        "Newsgroups: happynews.users");

        assertThat(header.get("fRoM"), equalTo("editor@example.org"));
    }


    @Test
    public void containsReturnsTrueForExistingField() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                        "Subject: The subject\n" +
                        "Newsgroups: happynews.users");

        assertThat(header.contains("From"), equalTo(true));
    }


    @Test
    public void containsReturnsFalseForNonexistingField() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                        "Subject: The subject\n" +
                        "Newsgroups: happynews.users");

        assertThat(header.contains("non_existent"), equalTo(false));
    }


    @Test
    public void containsIsCaseInsensitive() {
        ArticleHeader header = new ArticleHeader(
                "From: editor@example.org\n" +
                        "Subject: The subject\n" +
                        "Newsgroups: happynews.users");

        assertThat(header.contains("fRoM"), equalTo(true));
    }


    @Test
    public void putAddsNewField() {
        ArticleHeader header = new ArticleHeader("");
        String old = header.put("from", "user");

        assertThat(old, is(nullValue()));
        assertThat(header.get("from"), equalTo("user"));
        assertThat(header.toString(), equalTo("from: user"));
    }


    @Test
    public void putsPrependsNewlineToNewFieldUnlessFirst() {
        ArticleHeader header = new ArticleHeader("");
        header.put("subject", "hello");
        header.put("from", "user");

        assertThat(header.toString(), equalTo("subject: hello\nfrom: user"));
    }


    @Test
    public void putsReplacesExistingField() {
        ArticleHeader header = new ArticleHeader("");
        header.put("subject", "hello");
        header.put("from", "user");
        String old = header.put("from", "other-user");

        assertThat(old, equalTo("user"));
        assertThat(header.get("subject"), equalTo("hello"));
        assertThat(header.get("from"), equalTo("other-user"));
        assertThat(header.toString(), containsString("subject: hello"));
        assertThat(header.toString(), containsString("from: other-user"));
    }
}

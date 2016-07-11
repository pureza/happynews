package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class StatCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failsWhenGivenTooManyParameters() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new StatCommand(user, "STAT 1 2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void statWithArticleIdFailsIfArticleIdIsInvalid() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new StatCommand(user, "STAT invalid-article-id", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void statWithArticleIdFailsIfArticleDoesntExist() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new StatCommand(user, "STAT <1@host>", server).process();

        assertThat(out.get(), containsString("430 no such article found"));
    }


    @Test
    public void statWithArticleIdWorksEvenIfNoCurrentNewsgroup() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");

        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");
        when(server.getArticle("<2@host>")).thenReturn(article);

        new StatCommand(user, "STAT <2@host>", server).process();

        assertThat(out.get(), equalTo("223 0 <2@host> article retrieved - request text separately\n"));
    }


    @Test
    public void statWithArticleIdWorksEvenIfCurrentNewsgroupIsAnother() throws IOException {
        User user = mockReader("reader");
        user.setCurrentGroup(mockNewsgroup("group", "3@example.org"));
        user.setCurrentArticleIndex(2);
        Supplier<String> out = mockInput(user, "");

        Editor editor = mockEditor("editor");
        Article article = mockArticle("2@host", editor, "Hello world", "happynews.users");
        when(server.getArticle("<2@host>")).thenReturn(article);

        new StatCommand(user, "STAT <2@host>", server).process();

        assertThat(out.get(), equalTo("223 0 <2@host> article retrieved - request text separately\n"));
        assertThat(user.getCurrentArticleIndex(), equalTo(2));
    }


    @Test
    public void statWithArticleIndexFailsIfNoCurrentNewsgroup() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");

        new StatCommand(user, "STAT 1", server).process();

        assertThat(out.get(), containsString("412 no newsgroup has been selected"));
    }


    @Test
    public void statWithArticleIndexFailsWhenArticleDoesNotExist() throws IOException {
        User user = mockReader("reader");
        user.setCurrentGroup(mockNewsgroup("group"));
        Supplier<String> out = mockInput(user, "");

        new StatCommand(user, "STAT 1", server).process();

        assertThat(out.get(), containsString("423 no such article number in this group"));
    }


    @Test
    public void statWithArticleIndexWorksWhenArticleExists() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("1@host", editor, "Hello world", "happynews.users");

        User user = mockReader("reader");
        user.setCurrentGroup(mockNewsgroup("happynews.users", article.getId()));
        Supplier<String> out = mockInput(user, "");

        when(server.getArticle("<1@host>")).thenReturn(article);

        new StatCommand(user, "STAT 1", server).process();

        assertThat(out.get(), equalTo("223 1 <1@host> article retrieved - request text separately\n"));
    }


    @Test
    public void currentArticleFailsIfNoCurrentNewsgroup() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");

        new StatCommand(user, "STAT", server).process();

        assertThat(out.get(), containsString("412 no newsgroup has been selected"));
    }


    @Test
    public void currentArticleFailsWhenNewsgroupIsEmpty() throws IOException {
        User user = mockReader("reader");
        user.setCurrentGroup(mockNewsgroup("group"));
        Supplier<String> out = mockInput(user, "");

        new StatCommand(user, "STAT", server).process();

        assertThat(out.get(), containsString("420 no current article has been selected"));
    }


    @Test
    public void currentArticleShowsCurrentArticleWhenArticleExists() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("1@host", editor, "Hello world", "happynews.users");

        User user = mockReader("reader");
        user.setCurrentGroup(mockNewsgroup("happynews.users", article.getId()));
        Supplier<String> out = mockInput(user, "");

        when(server.getArticle("<1@host>")).thenReturn(article);

        new StatCommand(user, "STAT", server).process();

        assertThat(out.get(), equalTo("223 1 <1@host> article retrieved - request text separately\n"));
    }
}
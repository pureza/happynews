package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class FindheaderCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failsWhenNotEnoughParameters() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new FindheaderCommand(user, "FINDHEADER arg1 arg2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failsWhenGivenTooManyParameters() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new FindheaderCommand(user, "FINDHEADER arg1 arg2 arg3 arg4", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void printsMatchingArticles() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("123@host", editor, "Hello, world", "happynews.users");
        Newsgroup happyNewsUsers = new Newsgroup("happynews.users", new Date(), singletonList(article.getId()));

        when(server.getGroup(happyNewsUsers.getName())).thenReturn(happyNewsUsers);
        when(server.getArticle(article.getId())).thenReturn(article);

        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new FindheaderCommand(user, "FINDHEADER happynews.users subject Hello", server).process();

        assertThat(out.get(), equalTo("231 List of matching articles follows\n<123@host>\n.\n"));
    }


    @Test
    public void doesntPrintUnmatchingArticles() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("123@host", editor, "Hello, world", "happynews.users");
        Newsgroup happyNewsUsers = new Newsgroup("happynews.users", new Date(), singletonList(article.getId()));

        when(server.getGroup(happyNewsUsers.getName())).thenReturn(happyNewsUsers);
        when(server.getArticle(article.getId())).thenReturn(article);

        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new FindheaderCommand(user, "FINDHEADER happynews.users subject xpto", server).process();

        assertThat(out.get(), equalTo("231 List of matching articles follows\n.\n"));
    }


    @Test
    public void ignoresNonexistentGroups() throws IOException {
        Editor editor = mockEditor("editor");
        Article article = mockArticle("123@host", editor, "Hello, world", "happynews.users");
        Newsgroup happyNewsUsers = new Newsgroup("happynews.users", new Date(), singletonList(article.getId()));

        when(server.getGroup(happyNewsUsers.getName())).thenReturn(happyNewsUsers);
        when(server.getArticle(article.getId())).thenReturn(article);

        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new FindheaderCommand(user, "FINDHEADER happynews.users,nonexistent subject Hello", server).process();

        assertThat(out.get(), equalTo("231 List of matching articles follows\n<123@host>\n.\n"));
    }
}
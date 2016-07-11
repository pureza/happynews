package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.function.Supplier;

import static io.github.pureza.happynews.Tests.date;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NewnewsCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when (server.getGroup("group1")).thenReturn(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        when (server.getGroup("group2")).thenReturn(new Newsgroup("group2", new Date(), singletonList("<1@host>")));

        Article article1 = mock(Article.class);
        when (article1.getId()).thenReturn("<1@host>");
        when (server.getArticle(article1.getId())).thenReturn(article1);
        when (article1.getDatePosted()).thenReturn(date(2003, 12, 10, 8, 20));

        Article article2 = mock(Article.class);
        when (article2.getId()).thenReturn("<2@host>");
        when (server.getArticle(article2.getId())).thenReturn(article2);
        when (article2.getDatePosted()).thenReturn(date(2001, 12, 10, 8, 20));
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failWhenInsufficientParameters() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewnewsCommand(user, "NEWNEWS arg1 arg2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failWhenTooManyParameters() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewnewsCommand(user, "NEWNEWS arg1 arg2 arg3 arg4", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failWhenDateFormatIsWrong() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewnewsCommand(user, "NEWNEWS group1 9301AB 123000", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void writesListOfArticles() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewnewsCommand(user, "NEWNEWS group1 010101 123000", server).process();

        assertThat(out.get(), containsString("230 list of new articles by message-id follows"));
        assertThat(out.get(), containsString("<1@host>"));
        assertThat(out.get(), containsString("<2@host>"));
    }


    @Test
    public void ignoresOldArticles() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewnewsCommand(user, "NEWNEWS group1 020101 123000", server).process();

        assertThat(out.get(), equalTo("230 list of new articles by message-id follows\n<1@host>\n.\n"));
    }


    @Test
    public void doesNotShowDuplicateArticlesPostedInMultipleNewsgroups() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewnewsCommand(user, "NEWNEWS group1,group2 020101 123000", server).process();

        assertThat(out.get(), equalTo("230 list of new articles by message-id follows\n<1@host>\n.\n"));
    }


    @Test
    public void ignoresNonexistentGroups() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewnewsCommand(user, "NEWNEWS nonexistent 020101 123000", server).process();

        assertThat(out.get(), equalTo("230 list of new articles by message-id follows\n.\n"));
    }
}

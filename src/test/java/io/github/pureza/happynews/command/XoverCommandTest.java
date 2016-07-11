package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.function.Supplier;

import static io.github.pureza.happynews.Tests.date;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XoverCommandTest extends AbstractTest {

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
    public void failWhenThereIsNoCurrentNewsgroup() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new XoverCommand(user, "XOVER 1", server).process();

        assertThat(out.get(), containsString("412 no newsgroup has been selected"));
    }


    @Test
    public void failWhenNewsgroupIsEmpty() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), emptyList()));

        new XoverCommand(user, "XOVER", server).process();

        assertThat(out.get(), containsString("420 no current article has been selected"));
    }


    @Test
    public void printsCurrentArticleWhenGivenNoParameters() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        Article article = mockArticle("2@host", user, "Hello, world", "group1");
        ArticleHeader header = article.getHeader();
        when (server.getArticle(article.getId())).thenReturn(article);

        new XoverCommand(user, "XOVER", server).process();

        assertThat(out.get(), equalTo("224 Overview information follows\n1\tHello, world\teditor@example.org\t"
                + header.get("Date") + "\t<2@host>\n.\n"));
    }


    @Test
    public void failWhenIndexIsTooSmall() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));

        new XoverCommand(user, "XOVER 0", server).process();

        assertThat(out.get(), containsString("420 No article(s) selected"));
    }


    @Test
    public void oneWillPrintTheFirstArticle() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        Article article = mockArticle("2@host", user, "Hello, world", "group1");
        ArticleHeader header = article.getHeader();
        when (server.getArticle(article.getId())).thenReturn(article);

        new XoverCommand(user, "XOVER 1", server).process();

        assertThat(out.get(), equalTo("224 Overview information follows\n1\tHello, world\teditor@example.org\t"
                + header.get("Date") + "\t<2@host>\n.\n"));
    }


    @Test
    public void failWhenStartIndexTooLarge() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));

        new XoverCommand(user, "XOVER 3", server).process();

        assertThat(out.get(), containsString("420 No article(s) selected"));
    }


    @Test
    public void failWhenIllegalIndex() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));

        new XoverCommand(user, "XOVER a", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void printsSelectedArticle() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        Article article = mockArticle("1@host", user, "Hello, world", "group1");
        ArticleHeader header = article.getHeader();
        when (server.getArticle(article.getId())).thenReturn(article);

        new XoverCommand(user, "XOVER 2", server).process();

        assertThat(out.get(), equalTo("224 Overview information follows\n2\tHello, world\teditor@example.org\t"
                + header.get("Date")+ "\t<1@host>\n.\n"));
    }


    @Test
    public void failWhenRangeEndsTooSmall() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));

        new XoverCommand(user, "XOVER 0-0", server).process();

        assertThat(out.get(), containsString("420 No article(s) selected"));
    }


    @Test
    public void failWhenRangeStartsTooLarge() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));

        new XoverCommand(user, "XOVER 10-20", server).process();

        assertThat(out.get(), containsString("420 No article(s) selected"));
    }


    @Test
    public void failWhenRangeIsWrong() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));

        new XoverCommand(user, "XOVER 2-1", server).process();

        assertThat(out.get(), containsString("420 No article(s) selected"));
    }

    @Test
    public void printsArticleInRange() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        Article article = mockArticle("1@host", user, "Hello, world", "group1");
        ArticleHeader header = article.getHeader();
        when (server.getArticle(article.getId())).thenReturn(article);

        new XoverCommand(user, "XOVER 2-2", server).process();

        assertThat(out.get(), equalTo("224 Overview information follows\n2\tHello, world\teditor@example.org\t"
                + header.get("Date")+ "\t<1@host>\n.\n"));
    }


    @Test
    public void printsArticleInRangeEvenIfStartRangeIsTooSmall() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        Article article = mockArticle("2@host", user, "Hello, world", "group1");
        ArticleHeader header = article.getHeader();
        when (server.getArticle(article.getId())).thenReturn(article);

        new XoverCommand(user, "XOVER 0-1", server).process();

        assertThat(out.get(), equalTo("224 Overview information follows\n1\tHello, world\teditor@example.org\t"
                + header.get("Date")+ "\t<2@host>\n.\n"));
    }


    @Test
    public void printsArticleInRangeEvenIfEndRangeIsTooLarge() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        Article article = mockArticle("1@host", user, "Hello, world", "group1");
        ArticleHeader header = article.getHeader();
        when (server.getArticle(article.getId())).thenReturn(article);

        new XoverCommand(user, "XOVER 2-2000", server).process();

        assertThat(out.get(), equalTo("224 Overview information follows\n2\tHello, world\teditor@example.org\t"
                + header.get("Date")+ "\t<1@host>\n.\n"));
    }


    @Test
    public void printsMultipleArticlesInRange() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setCurrentGroup(new Newsgroup("group1", new Date(), asList("<2@host>", "<1@host>")));
        Article article1 = mockArticle("1@host", user, "Hello, world", "group1");
        ArticleHeader header1 = article1.getHeader();
        when (server.getArticle(article1.getId())).thenReturn(article1);

        Article article2 = mockArticle("2@host", user, "Hello, world 2", "group1");
        ArticleHeader header2 = article1.getHeader();
        when (server.getArticle(article2.getId())).thenReturn(article2);

        new XoverCommand(user, "XOVER 1-2", server).process();

        assertThat(out.get(), equalTo("224 Overview information follows\n" +
                "1\tHello, world 2\teditor@example.org\t" + header2.get("Date")+ "\t<2@host>\n" +
                "2\tHello, world\teditor@example.org\t" + header1.get("Date")+ "\t<1@host>\n" +
                ".\n"));
    }
}

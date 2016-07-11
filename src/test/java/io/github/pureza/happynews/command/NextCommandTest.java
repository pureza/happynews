package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static io.github.pureza.happynews.Tests.date;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class NextCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failWhenUserIsNotWithinGroup() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NextCommand(user, "NEXT", server).process();

        assertThat(out.get(), containsString("412 no newsgroup has been selected"));
    }


    @Test
    public void failWhenNewsgroupIsEmpty() throws Exception {
        Newsgroup group = new Newsgroup("group", date(2004, 10, 3, 10, 20), emptyList());

        Reader user = mockReader("reader");
        user.setCurrentGroup(group);
        Supplier<String> out = mockInput(user, "");
        new NextCommand(user, "NEXT", server).process();

        assertThat(out.get(), containsString("420 no current article has been selected"));
    }


    @Test
    public void failWhenCurrentArticleIsTheFirst() throws Exception {
        Newsgroup group = new Newsgroup("group", date(2004, 10, 3, 10, 20), singletonList("<123@host>"));

        Reader user = mockReader("reader");
        user.setCurrentGroup(group);
        user.setCurrentArticleIndex(2);
        Supplier<String> out = mockInput(user, "");
        new NextCommand(user, "NEXT", server).process();

        assertThat(out.get(), containsString("421 no next article in this group"));
        assertThat(user.getCurrentGroup(), equalTo(group));
    }


    @Test
    public void movesToMextArticleOnSuccess() throws Exception {
        Newsgroup group = new Newsgroup("group", date(2004, 10, 3, 10, 20), asList("<1@host>", "<2@host>"));

        when(server.getArticle("<2@host>")).thenReturn(new Article("<2@host>", config.articlesHome()));

        Reader user = mockReader("reader");
        user.setCurrentGroup(group);
        user.setCurrentArticleIndex(1);
        Supplier<String> out = mockInput(user, "");
        new NextCommand(user, "NEXT", server).process();

        assertThat(out.get(), containsString("223 2 <2@host> article retrieved - request text separately"));
        assertThat(user.getCurrentArticleIndex(), equalTo(2));
    }
}
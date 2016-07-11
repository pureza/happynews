package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

import static io.github.pureza.happynews.Tests.date;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class ListCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when (server.groups()).thenReturn(new HashMap<String, Newsgroup>() {{
            put("group1", new Newsgroup("group1", date(1998, 10, 3, 10, 0), emptyList()));
            put("group2", new Newsgroup("group2", date(2004, 10, 3, 10, 20), singletonList("123@host")));
            put("group3", new Newsgroup("group3", date(2005, 7, 2, 11, 30), emptyList()));
        }});
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void writesListOfNewNewsgroups() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new ListCommand(user, "LIST", server).process();

        assertThat(out.get(), containsString("215 list of newsgroups follows"));
        assertThat(out.get(), containsString("group1 0 1 n"));
        assertThat(out.get(), containsString("group2 1 1 n"));
        assertThat(out.get(), containsString("group3 0 1 n"));
    }


    @Test
    public void editorsCanPost() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new ListCommand(user, "LIST", server).process();

        assertThat(out.get(), containsString("215 list of newsgroups follows"));
        assertThat(out.get(), containsString("group1 0 1 y"));
        assertThat(out.get(), containsString("group2 1 1 y"));
        assertThat(out.get(), containsString("group3 0 1 y"));
    }
}

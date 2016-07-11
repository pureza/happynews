package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static io.github.pureza.happynews.Tests.date;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class GroupCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failWhenInsufficientParameters() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new GroupCommand(user, "GROUP", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failWhenGroupDoesNotExist() throws Exception {
        when(server.getGroup("group")).thenReturn(null);

        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new GroupCommand(user, "GROUP group", server).process();

        assertThat(out.get(), containsString("411 no such news group"));
    }


    @Test
    public void groupSwitchesUserToNewGroup() throws Exception {
        Newsgroup group = new Newsgroup("group", date(2004, 10, 3, 10, 20), singletonList("123@host"));

        when(server.getGroup("name"))
                .thenReturn(group);

        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new GroupCommand(user, "GROUP name", server).process();

        assertThat(out.get(), containsString("211 1 1 1 group group selected"));
        assertThat(user.getCurrentGroup(), equalTo(group));
    }
}

package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class NewgroupCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failForReaders() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new NewgroupCommand(user, "NEWGROUP name", server).process();

        assertThat(out.get(), containsString("450 create newsgroup not allowed"));
    }


    @Test
    public void failForEditors() throws Exception {
        Editor user = mockEditor("reader");
        Supplier<String> out = mockInput(user, "");
        new NewgroupCommand(user, "NEWGROUP name", server).process();

        assertThat(out.get(), containsString("450 create newsgroup not allowed"));
    }


    @Test
    public void failWhenInsufficientParameters() throws Exception {
        Admin user = mockAdmin("reader");
        Supplier<String> out = mockInput(user, "");
        new NewgroupCommand(user, "NEWGROUP", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failWhenCreatingFails() throws Exception {
        when (server.createGroup("name")).thenReturn(false);

        Admin user = mockAdmin("reader");
        Supplier<String> out = mockInput(user, "");
        new NewgroupCommand(user, "NEWGROUP name", server).process();

        assertThat(out.get(), containsString("451 create failed"));
    }


    @Test
    public void createNewsgroup() throws Exception {
        when (server.createGroup("name")).thenReturn(true);

        Admin user = mockAdmin("reader");
        Supplier<String> out = mockInput(user, "");
        new NewgroupCommand(user, "NEWGROUP name", server).process();

        assertThat(out.get(), containsString("250 group created"));
    }
}

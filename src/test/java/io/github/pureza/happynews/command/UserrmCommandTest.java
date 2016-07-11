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

public class UserrmCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failForReader() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new UserrmCommand(user, "USERRM name", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failForEditor() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new UserrmCommand(user, "USERRM name", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserrmCommand(user, "USERRM", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserrmCommand(user, "USERRM name arg2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failWhenUserIsNotRemoved() throws IOException {
        when (server.removeUser("name")).thenReturn(false);

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserrmCommand(user, "USERRM name", server).process();

        assertThat(out.get(), containsString("482 user not removed - perhaps user is online?"));
    }


    @Test
    public void succeedWhenUserIsRemoved() throws IOException {
        when (server.removeUser("name")).thenReturn(true);

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserrmCommand(user, "USERRM name", server).process();

        assertThat(out.get(), containsString("282 user removed"));
    }
}

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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class UseraddCommandTest extends AbstractTest {

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
        new UseraddCommand(user, "USERADD name pass reader", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failForEditor() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new UseraddCommand(user, "USERADD name pass reader", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UseraddCommand(user, "USERADD name pass", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UseraddCommand(user, "USERADD name pass reader arg4", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnNonexistentRole() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UseraddCommand(user, "USERADD name pass cleaner", server).process();

        assertThat(out.get(), containsString("481 operation not performed"));
    }


    @Test
    public void failWhenUserAlreadyExists() throws IOException {
        when (server.addUser(anyObject())).thenReturn(false);

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UseraddCommand(user, "USERADD name pass reader", server).process();

        assertThat(out.get(), containsString("481 user already exists"));
    }


    @Test
    public void createsUserWhenSuccessful() throws IOException {
        when (server.addUser(anyObject())).thenReturn(true);

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UseraddCommand(user, "USERADD name pass reader", server).process();

        assertThat(out.get(), containsString("281 user added"));
    }


    @Test
    public void roleIsCaseInsensitive() throws IOException {
        when (server.addUser(anyObject())).thenReturn(true);

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UseraddCommand(user, "USERADD name pass ReAdEr", server).process();

        assertThat(out.get(), containsString("281 user added"));
    }
}

package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.Reader;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class UserchCommandTest extends AbstractTest {

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
        new UserchCommand(user, "USERCH name editor", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failForEditor() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new UserchCommand(user, "USERCH name reader", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserchCommand(user, "USERCH name", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserchCommand(user, "USERCH name reader arg3", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failWhenRoleIsInvalid() throws IOException {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserchCommand(user, "USERCH name invalid_role", server).process();

        assertThat(out.get(), containsString("483 role unchanged"));
    }


    @Test
    public void failWhenUserIsNotUpdated() throws IOException {
        when (server.changeUserRole("name", User.Role.READER)).thenReturn(false);

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserchCommand(user, "USERCH name reader", server).process();

        assertThat(out.get(), containsString("483 role unchanged"));
    }


    @Test
    public void succeedWhenUserIsUpdated() throws IOException {
        when (server.changeUserRole("name", User.Role.READER)).thenReturn(true);

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new UserchCommand(user, "USERCH name reader", server).process();

        assertThat(out.get(), containsString("283 role changed"));
    }
}

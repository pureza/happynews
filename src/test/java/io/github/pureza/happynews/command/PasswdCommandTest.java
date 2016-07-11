package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
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

public class PasswdCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failOnNotEnoughParameters() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new PasswdCommand(user, "PASSWD", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new PasswdCommand(user, "PASSWD user pass arg3", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void userCanChangeOwnPassword() throws Exception {
        Reader user = mockReader("reader");
        when(server.changeUserPassword(user.getUsername(), "new-pass")).thenReturn(true);

        Supplier<String> out = mockInput(user, "");
        new PasswdCommand(user, "PASSWD new-pass", server).process();

        assertThat(out.get(), containsString("284 password changed"));
    }


    @Test
    public void userCanChangeOwnPasswordWithUsername() throws Exception {
        Reader user = mockReader("reader");
        when(server.getUser(user.getUsername())).thenReturn(user);
        when(server.changeUserPassword(user.getUsername(), "new-pass")).thenReturn(true);

        Supplier<String> out = mockInput(user, "");
        new PasswdCommand(user, "PASSWD reader new-pass", server).process();

        assertThat(out.get(), containsString("284 password changed"));
    }


    @Test
    public void ownPasswordChangeMayFail() throws Exception {
        Reader user = mockReader("reader");
        when(server.changeUserPassword(user.getUsername(), "new-pass")).thenReturn(false);

        Supplier<String> out = mockInput(user, "");
        new PasswdCommand(user, "PASSWD new-pass", server).process();

        assertThat(out.get(), containsString("484 password not changed - maybe it is invalid?"));
    }


    @Test
    public void readersCantChangeOtherUserPasswords() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new PasswdCommand(user, "PASSWD admin new-pass", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void editorsCantChangeOtherUserPasswords() throws Exception {
        User user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new PasswdCommand(user, "PASSWD admin new-pass", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failsWhenUserDoesntExist() throws Exception {
        when (server.getUser("unknown")).thenReturn(null);

        User admin = mockAdmin("admin");
        Supplier<String> out = mockInput(admin, "");
        new PasswdCommand(admin, "PASSWD unknown new-pass", server).process();

        assertThat(out.get(), containsString("484 user doesn't exist"));
    }


    @Test
    public void adminsCanChangeOtherUserPasswords() throws Exception {
        User reader = mockReader("reader");

        when(server.getUser(reader.getUsername())).thenReturn(reader);
        when(server.changeUserPassword("reader", "new-pass")).thenReturn(true);

        User admin = mockAdmin("admin");
        Supplier<String> out = mockInput(admin, "");
        new PasswdCommand(admin, "PASSWD reader new-pass", server).process();

        assertThat(out.get(), containsString("284 password changed"));
    }


    @Test
    public void failsWhenNewPasswordIsInvalid() throws Exception {
        User reader = mockReader("reader");

        when(server.getUser(reader.getUsername())).thenReturn(reader);
        when(server.changeUserPassword("reader", "invalid-password")).thenReturn(false);

        User admin = mockAdmin("admin");
        Supplier<String> out = mockInput(admin, "");
        new PasswdCommand(admin, "PASSWD reader invalid-password", server).process();

        assertThat(out.get(), containsString("484 password not changed - maybe it is invalid?"));
    }
}

package io.github.pureza.happynews.server;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientHandlerTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void authenticateFailsForWrongPassword() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "AUTHINFO USER reader\nAUTHINFO PASSWORD wrong-password");

        when (server.login(user.getUsername(), "wrong-password")).thenReturn(null);

        ClientHandler handler = new ClientHandler(user.getClientSocket(), server);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(user.getClientSocket().getInputStream())));
             PrintStream outStream = new PrintStream(user.getClientSocket().getOutputStream(), true)) {
            handler.authenticate(in, outStream);
            throw new RuntimeException("authenticate() must fail!");
        } catch (InvalidLoginException ex) {
            // All good!
        }

        assertThat(out.get(), equalTo("480 server ready - authentication required\n381 password please...\n482 Invalid login\n"));
    }


    @Test
    public void authenticateSucceedsForCorrectPassword() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "AUTHINFO USER reader\nAUTHINFO PASSWORD reader");

        when (server.login(user.getUsername(), user.getPassword())).thenReturn(user);

        ClientHandler handler = new ClientHandler(user.getClientSocket(), server);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(user.getClientSocket().getInputStream())));
             PrintStream outStream = new PrintStream(user.getClientSocket().getOutputStream(), true)) {
            handler.authenticate(in, outStream);
        } catch (InvalidLoginException ex) {
            // All good!
            throw new RuntimeException("authenticate() must succeed!");
        }

        assertThat(out.get(), equalTo("480 server ready - authentication required\n381 password please...\n281 Authentication accepted\n"));
    }


    @Test
    public void quitQuitsAndClosesSocket() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "AUTHINFO USER reader\nAUTHINFO PASSWORD reader\nquit");

        when (server.login(user.getUsername(), user.getPassword())).thenReturn(user);

        ClientHandler handler = new ClientHandler(user.getClientSocket(), server);
        handler.run();

        assertThat(out.get(), equalTo("480 server ready - authentication required\n381 password please...\n281 Authentication accepted\n"));
        verify(user.getClientSocket()).close();
    }


    @Test
    public void handlerThrowsErrorOnMissingCommands() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "AUTHINFO USER reader\nAUTHINFO PASSWORD reader\nBLAH\nquit");

        when (server.login(user.getUsername(), user.getPassword())).thenReturn(user);

        ClientHandler handler = new ClientHandler(user.getClientSocket(), server);
        handler.run();

        assertThat(out.get(), equalTo("480 server ready - authentication required\n381 password please...\n281 Authentication accepted\n500 BLAH: Command not recognized\n"));
    }


    @Test
    public void handlerSkipsEmptyLines() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "AUTHINFO USER reader\nAUTHINFO PASSWORD reader\n\nquit");

        when (server.login(user.getUsername(), user.getPassword())).thenReturn(user);

        ClientHandler handler = new ClientHandler(user.getClientSocket(), server);
        handler.run();

        assertThat(out.get(), equalTo("480 server ready - authentication required\n381 password please...\n281 Authentication accepted\n"));
    }


    @Test
    public void handlerExecutesCommands() throws Exception {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "AUTHINFO USER reader\nAUTHINFO PASSWORD reader\nSLAVE\nquit");

        when (server.login(user.getUsername(), user.getPassword())).thenReturn(user);

        ClientHandler handler = new ClientHandler(user.getClientSocket(), server);
        handler.run();

        assertThat(out.get(), equalTo("480 server ready - authentication required\n381 password please...\n281 Authentication accepted\n202 slave status noted\n"));
    }
}

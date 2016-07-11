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
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class UserlistCommandTest extends AbstractTest {

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
        new UserlistCommand(user, "USERLIST", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failForEditor() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new UserlistCommand(user, "USERLIST", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void printsListOfUsersWhenThereAreNoUsers() throws Exception {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");

        when (server.users()).thenReturn(Collections.emptyMap());

        new UserlistCommand(user, "USERLIST", server).process();

        assertThat(out.get(), equalTo("280 User list follows\n.\n"));
    }


    @Test
    public void printsListOfUsers() throws Exception {
        Reader reader = mockReader("reader");

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");

        when (server.users()).thenReturn(new HashMap<String, User>() {{
            put(reader.getUsername(), reader);
            put(user.getUsername(), user);
        }});

        new UserlistCommand(user, "USERLIST", server).process();
        assertThat(out.get(), containsString("280 User list follows"));
        assertThat(out.get(), containsString("reader Reader"));
        assertThat(out.get(), containsString("admin Admin 127.0.0.1"));
    }


    @Test
    public void printsOnlyOnlineUsersWithParameter() throws Exception {
        Reader reader = mockReader("reader");

        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");

        when (server.users()).thenReturn(new HashMap<String, User>() {{
            put(reader.getUsername(), reader);
            put(user.getUsername(), user);
        }});

        new UserlistCommand(user, "USERLIST --onlineonly", server).process();
        assertThat(out.get(), equalTo("280 User list follows\nadmin Admin 127.0.0.1\n.\n"));
    }
}

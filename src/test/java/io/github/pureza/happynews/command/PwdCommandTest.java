package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class PwdCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void readerHasNoPermission() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new PwdCommand(user, "PWD", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void printsWorkingDirectoryForEditors() throws Exception {
        Reader user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new PwdCommand(user, "PWD", server).process();

        assertThat(out.get(), containsString("289 /editor"));
    }


    @Test
    public void printsWorkingDirectoryForAdmins() throws Exception {
        Reader user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");
        new PwdCommand(user, "PWD", server).process();

        assertThat(out.get(), containsString("289 /admin"));
    }


    @Test
    public void printsWorkingDirectoryWhenUserChangesPath() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        user.setPath(user.getPath().resolve("subdir"));
        new PwdCommand(user, "PWD", server).process();

        assertThat(out.get(), containsString("289 /editor/subdir"));
    }
}

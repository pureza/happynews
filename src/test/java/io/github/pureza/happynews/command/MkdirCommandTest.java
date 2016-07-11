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
import static org.mockito.Mockito.mock;

public class MkdirCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failOnInsufficientPrivileges() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new MkdirCommand(user, "MKDIR", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new MkdirCommand(user, "MKDIR", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new MkdirCommand(user, "MKDIR arg1 arg2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void createsDirectoryForEditors() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new MkdirCommand(user, "MKDIR dir", server).process();

        assertThat(out.get(), containsString("287 directory created"));
    }


    @Test
    public void createsSubdir() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new MkdirCommand(user, "MKDIR dir", server).process();
        new MkdirCommand(user, "MKDIR dir/inner", server).process();

        assertThat(out.get(), containsString("287 directory created\n287 directory created"));
    }


    @Test
    public void failWhenDirectoryAlreadyExists() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new MkdirCommand(user, "MKDIR dir", server).process();
        new MkdirCommand(user, "MKDIR dir", server).process();

        assertThat(out.get(), containsString("287 directory created"));
        assertThat(out.get(), containsString("487 directory not created"));
    }


    @Test
    public void failWhenCreatingDirectoryOutsideHome() throws Exception {
        Editor user = mockEditor("reader");
        Supplier<String> out = mockInput(user, "");
        new MkdirCommand(user, "MKDIR ../outer", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }
}

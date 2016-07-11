package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class RmdirCommandTest extends AbstractTest {

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
        new RmdirCommand(user, "RMDIR", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmdirCommand(user, "RMDIR", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmdirCommand(user, "RMDIR arg1 arg2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnCurrentWorkingDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmdirCommand(user, "RMDIR .", server).process();

        assertThat(out.get(), containsString("491 can't delete current directory"));
    }


    @Test
    public void failOnDeletingDirectoriesOutsideHome() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmdirCommand(user, "RMDIR ../", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failWhenDirectoryDoesNotExist() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmdirCommand(user, "RMDIR child", server).process();

        assertThat(out.get(), containsString("491 not a directory"));
    }


    @Test
    public void failWhenIsNotDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Files.createFile(user.getHome().resolve("file"));
        new RmdirCommand(user, "RMDIR file", server).process();

        assertThat(out.get(), containsString("491 not a directory"));
    }


    @Test
    public void failWhenDirectoryIsNotEmpty() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Files.createDirectory(user.getHome().resolve("child"));
        Files.createFile(user.getHome().resolve("child").resolve("file"));
        new RmdirCommand(user, "RMDIR child", server).process();

        assertThat(out.get(), containsString("491 directory not removed"));
    }


    @Test
    public void removesExistingDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        Files.createDirectory(user.getHome().resolve("child"));
        new RmdirCommand(user, "RMDIR child", server).process();

        assertThat(out.get(), containsString("291 directory removed"));
    }
}

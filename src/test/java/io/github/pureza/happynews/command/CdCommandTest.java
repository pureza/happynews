package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CdCommandTest extends AbstractTest {

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
        new CdCommand(user, "CD .", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new CdCommand(user, "CD", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new CdCommand(user, "CD arg1 arg2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failWhenPathDoesNotExist() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new CdCommand(user, "CD nonexistent", server).process();

        assertThat(out.get(), containsString("485 directory not changed"));
    }


    @Test
    public void movesIntoExistingDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        Files.createDirectory(user.getHome().resolve("child"));
        new CdCommand(user, "CD child", server).process();

        assertThat(out.get(), containsString("285 directory changed"));
    }


    @Test
    public void movesIntoInnerExistingDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        Files.createDirectories(user.getHome().resolve("child").resolve("sub"));
        new CdCommand(user, "CD child/sub", server).process();

        assertThat(out.get(), containsString("285 directory changed"));
    }


    @Test
    public void movesIntoOuterExistingDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new CdCommand(user, "CD ../", server).process();

        assertThat(out.get(), containsString("285 directory changed"));
    }


    @Test
    public void movesIntoOtherUserHome() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Admin admin = mockAdmin("admin");

        new CdCommand(user, "CD ../admin", server).process();

        assertThat(out.get(), containsString("285 directory changed"));
    }


    @Test
    public void doesntGoAboveUserRoot() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new CdCommand(user, "CD ../../", server).process();

        assertThat(out.get(), containsString("285 directory changed"));
        assertThat(user.getPath(), equalTo(config.usersHome()));
    }


    @Test
    public void failWhenIsNotDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Files.createDirectory(user.getHome().resolve("child"));
        Files.createFile(user.getHome().resolve("child").resolve("file"));

        new CdCommand(user, "CD child/file", server).process();

        assertThat(out.get(), containsString("485 directory not changed"));
    }
}

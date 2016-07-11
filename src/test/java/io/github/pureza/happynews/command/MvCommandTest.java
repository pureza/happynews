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
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MvCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Path a = Files.createDirectories(config.usersHome().resolve("editor").resolve("a"));
        Path b = Files.createDirectories(config.usersHome().resolve("editor").resolve("b"));
        Path c = Files.createDirectories(config.usersHome().resolve("editor").resolve("c"));
        Files.createFile(a.resolve("1@host"));
        Files.createFile(c.resolve("1@host"));
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failOnInsufficientPrivileges() throws Exception {
        Reader user = mockReader("editor");
        Supplier<String> out = mockInput(user, "");
        new MvCommand(user, "MV a/<1@host> b", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new MvCommand(user, "MV arg1", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new MvCommand(user, "MV arg1 arg2 arg3", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnInvalidArticleId() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        new MvCommand(user, "MV a/1@host b", server).process();

        // ... after
        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnArticleNotFound() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        new MvCommand(user, "MV a/<2@host> b", server).process();

        // ... after
        assertThat(out.get(), containsString("488 article not found"));
    }


    @Test
    public void failOnPathNotFound() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        new MvCommand(user, "MV a/<1@host> nonexistent", server).process();

        // ... after
        assertThat(out.get(), containsString("488 path not found"));
    }


    @Test
    public void movesArticle() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        // before...
        assertThat(Files.exists(user.getHome().resolve("a").resolve("1@host")), is(true));
        assertThat(Files.exists(user.getHome().resolve("b").resolve("1@host")), is(false));

        new MvCommand(user, "MV a/<1@host> b", server).process();

        // ... after
        assertThat(out.get(), containsString("288 article moved"));
        assertThat(Files.exists(user.getHome().resolve("a").resolve("1@host")), is(false));
        assertThat(Files.exists(user.getHome().resolve("b").resolve("1@host")), is(true));
    }


    @Test
    public void succeedsWhenMovingArticleToItself() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        // before...
        assertThat(Files.exists(user.getHome().resolve("a").resolve("1@host")), is(true));

        new MvCommand(user, "MV a/<1@host> a/", server).process();

        // ... after
        assertThat(out.get(), containsString("288 article moved"));
        assertThat(Files.exists(user.getHome().resolve("a").resolve("1@host")), is(true));
    }


    @Test
    public void failsWhenMovingArticleOutsideHome() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        // before...
        assertThat(Files.exists(user.getHome().resolve("a").resolve("1@host")), is(true));

        new MvCommand(user, "MV a/<1@host> ../", server).process();

        // ... after
        assertThat(out.get(), containsString("502 Permission denied"));
    }


    @Test
    public void failsWhenMovingArticleFromAnotherUser() throws Exception {
        Admin user = mockAdmin("admin");
        Supplier<String> out = mockInput(user, "");

        new MvCommand(user, "MV ../editor/a/<1@host> .", server).process();

        assertThat(out.get(), containsString("502 Permission denied"));
    }


    @Test
    public void failsWhenOverwritingExistingFile() throws Exception {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        new MvCommand(user, "MV a/<1@host> c/", server).process();

        assertThat(out.get(), containsString("488 move failed"));
    }
}

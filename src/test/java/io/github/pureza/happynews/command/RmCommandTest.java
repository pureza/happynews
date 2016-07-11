package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
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

public class RmCommandTest extends AbstractTest {

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
        new RmCommand(user, "RM", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failOnNotEnoughParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmCommand(user, "RM", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnTooManyParameters() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmCommand(user, "RM arg1 arg2", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnInvalidArticleId() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmCommand(user, "RM invalid-article-id", server).process();

        assertThat(out.get(), containsString("501 command syntax error"));
    }


    @Test
    public void failOnNonexistentArticle() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmCommand(user, "RM <1@host>", server).process();

        assertThat(out.get(), containsString("490 no such article"));
    }


    @Test
    public void failOnDeletingArticlesOutsideHome() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new RmCommand(user, "RM ../other-user/<2@host>", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void removesExistingArticles() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Path inner = Files.createDirectories(user.getHome().resolve("inner"));
        Files.createFile(inner.resolve("2@host"));

        new RmCommand(user, "RM inner/<2@host>", server).process();

        assertThat(out.get(), containsString("290 article removed from your local directory"));
    }
}

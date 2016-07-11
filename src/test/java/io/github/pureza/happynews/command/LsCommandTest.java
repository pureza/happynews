package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
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
import static org.mockito.Mockito.when;

public class LsCommandTest extends AbstractTest {

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
        new LsCommand(user, "LS", server).process();

        assertThat(out.get(), containsString("502 permission denied"));
    }


    @Test
    public void failWhenPathDoesNotExist() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new LsCommand(user, "LS nonexistent", server).process();

        assertThat(out.get(), containsString("486 path doesn't exist"));
    }


    @Test
    public void listsCurrentDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        Files.createDirectories(user.getHome().resolve("child"));
        new LsCommand(user, "LS", server).process();

        assertThat(out.get(), containsString("286 list follows\nchild/\n.\n"));
    }


    @Test
    public void listsExistingDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        Files.createDirectories(user.getHome().resolve("child").resolve("sub"));
        new LsCommand(user, "LS child", server).process();

        assertThat(out.get(), containsString("286 list follows\nsub/\n.\n"));
    }


    @Test
    public void listsExistingInnerDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Path sub = user.getHome().resolve("child").resolve("sub");
        Files.createDirectories(sub);
        Files.createFile(sub.resolve("1@host"));

        new LsCommand(user, "LS child/sub", server).process();

        assertThat(out.get(), containsString("286 list follows\n<1@host>\n.\n"));
    }


    @Test
    public void listsOuterExistingDirectory() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new LsCommand(user, "LS ../", server).process();

        assertThat(out.get(), containsString("286 list follows\n" +
                "editor/\n" +
                ".\n"));
    }


    @Test
    public void listsOtherUserHome() throws IOException {
        Editor user = mockEditor("editor");

        Admin admin = mockAdmin("admin");
        Files.createDirectories(admin.getHome().resolve("admin-things"));

        Supplier<String> out = mockInput(user, "");
        new LsCommand(user, "LS ../admin", server).process();

        assertThat(out.get(), containsString("286 list follows\n" +
                "admin-things/\n" +
                ".\n"));
    }


    @Test
    public void doesntListAboveUserRoot() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");
        new LsCommand(user, "LS ../../", server).process();

        assertThat(out.get(), containsString("286 list follows\neditor/\n.\n"));
    }


    @Test
    public void listsDirectoriesAndFiles() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Path home = user.getHome();
        Files.createDirectories(home.resolve("dir"));
        Files.createFile(home.resolve("1@host"));

        new LsCommand(user, "LS .", server).process();

        assertThat(out.get(), containsString("286 list follows"));
        assertThat(out.get(), containsString("<1@host>"));
        assertThat(out.get(), containsString("dir/"));
    }


    @Test
    public void detailedListingShowsCurrentWorkingDirectoryByDefault() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Path home = user.getHome();
        Files.createDirectories(home.resolve("dir"));
        Article article = mockArticle("1@host", user, "hello world from HappyNews", "happynews.users");

        when (server.getArticle(article.getId())).thenReturn(article);
        new LsCommand(user, "LS -d", server).process();

        assertThat(out.get(), containsString("286 list follows"));
        assertThat(out.get(), containsString("<1@host> happynews.users hello world from HappyNews"));
        assertThat(out.get(), containsString("dir/"));
    }


    @Test
    public void showsDetailedListingOfOtherDirectories() throws IOException {
        Editor user = mockEditor("editor");
        Supplier<String> out = mockInput(user, "");

        Path home = user.getHome();
        Files.createDirectories(home.resolve("dir").resolve("sub"));
        user.setPath(home.resolve("dir"));

        Article article = mockArticle("2@host", user, "hello world from HappyNews", "happynews.users");
        user.setPath(home);

        when (server.getArticle(article.getId())).thenReturn(article);
        new LsCommand(user, "LS -d dir", server).process();

        assertThat(out.get(), containsString("286 list follows"));
        assertThat(out.get(), containsString("<2@host> happynews.users hello world from HappyNews"));
        assertThat(out.get(), containsString("sub/"));
    }
}

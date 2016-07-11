package io.github.pureza.happynews.server;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NNTPServerTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Create an empty file, so that it doesn't fail
        Files.createFile(config.usersFile());
        this.server = new NNTPServer(mock(ServerSocket.class), config);
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void loginFailsForUnknownUser() throws Exception {
        assertThat(server.login("nonexistent_user", "password"), is(nullValue()));
    }


    @Test
    public void loginFailsForWrongPassword() throws Exception {
        User user = mockReader("reader");
        server.addUser(user);

        assertThat(server.login(user.getUsername(), user.getPassword() + "extra"), is(nullValue()));
    }


    @Test
    public void loginFailsIfUserIsAlreadyOnline() throws Exception {
        User user = mockReader("reader");
        server.addUser(user);

        user.setClientSocket(mock(Socket.class));

        assertThat(user.isOnline(), is(true));
        assertThat(server.login(user.getUsername(), user.getPassword()), is(nullValue()));
    }


    @Test
    public void loginSucceedsForCorrectPassword() throws Exception {
        User user = mockReader("reader");
        String originalPassword = user.getPassword();
        server.addUser(user);

        assertThat(server.login(user.getUsername(), originalPassword), is(user));
    }


    @Test
    public void postArticleFailsIfArticleCantBePosted() throws Exception {
        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("Invalid: header");
        String body = "A perfect body";

        assertThat(server.postArticle(header, body, user), is(false));
    }


    @Test
    public void postArticleAddsArticleToNewsgroups() throws IOException {
        server.createGroup("happynews.users");
        server.createGroup("happynews.dev");

        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users, happynews.dev");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        assertThat(server.postArticle(header, body, user), is(true));

        Newsgroup happyNewsUsers = server.getGroup("happynews.users");
        Newsgroup happyNewsDev = server.getGroup("happynews.dev");

        assertThat(happyNewsUsers.articles(), is(singletonList("<1@host.org>")));
        assertThat(happyNewsDev.articles(), is(singletonList("<1@host.org>")));
    }


    @Test
    public void postArticleIgnoresUnknownNewsgroups() throws Exception {
        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users"); // Doesn't exist
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        assertThat(server.postArticle(header, body, user), is(true));
    }


    @Test
    public void postArticleSetsDateHeaderField() throws Exception {
        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        Date before = new Date();
        assertThat(server.postArticle(header, body, user), is(true));

        SimpleDateFormat dateFormatter = new SimpleDateFormat("E, dd MMM yy HH:mm:ss");
        Date date = dateFormatter.parse(server.getArticle("<1@host.org>").getHeader().get("date"));

        // < 1 second difference
        assertThat(Math.abs(date.getTime() - before.getTime()) < 1000, is(true));
    }
}

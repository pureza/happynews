package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class PostCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void failsWhenInsufficientPrivileges() throws IOException {
        User user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new PostCommand(user, "POST", server).process();

        assertThat(out.get(), containsString("440 posting not allowed"));
    }


    @Test
    public void postFailsOnEmptyMessage() throws IOException {
        Editor editor = mockEditor("editor");
        Supplier<String> out = mockInput(editor, ".");
        new PostCommand(editor, "POST", server).process();

        assertThat(out.get(), containsString("441 posting failed: blank message?"));
    }


    @Test
    public void postPostsValidArticle() throws IOException {
        String header = "From: happy@example.org\n" +
                "Subject: hello world from HappyNews\n" +
                "Newsgroups: happynews.users";

        String body = "This is the body";

        Editor editor = mockEditor("editor");
        Supplier<String> out = mockInput(editor, header + "\n\n" + body + "\n.\n");

        // XXX Note that this tests whether the last \n is removed from the body
        when (server.postArticle(any(), eq(body), eq(editor))).thenReturn(true);

        new PostCommand(editor, "POST", server).process();

        assertThat(out.get(), containsString("340 send article to be posted. End with <CR-LF>.<CR-LF>"));
        assertThat(out.get(), containsString("240 article posted ok"));
    }


    @Test
    public void postPostsArticleWithNoBody() throws IOException {
        String header = "From: happy@example.org\n" +
                "Subject: hello world from HappyNews\n" +
                "Newsgroups: happynews.users";

        Editor editor = mockEditor("editor");
        Supplier<String> out = mockInput(editor, header + "\n\n" + ".\n");

        when (server.postArticle(any(), eq(""), eq(editor))).thenReturn(true);

        new PostCommand(editor, "POST", server).process();

        assertThat(out.get(), containsString("340 send article to be posted. End with <CR-LF>.<CR-LF>"));
        assertThat(out.get(), containsString("240 article posted ok"));
    }


    @Test
    public void failsWhenArticleCantBePosted() throws IOException {
        String header = "from: the header";
        String body = "This is the body";

        Editor editor = mockEditor("editor");
        Supplier<String> out = mockInput(editor, header + "\n\n" + body + "\n.\n");

        when (server.postArticle(any(), eq(body), eq(editor))).thenReturn(false);

        new PostCommand(editor, "POST", server).process();

        assertThat(out.get(), containsString("340 send article to be posted. End with <CR-LF>.<CR-LF>"));
        assertThat(out.get(), containsString("441 posting failed: missing headers?"));
    }
}
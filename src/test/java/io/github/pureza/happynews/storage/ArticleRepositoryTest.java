package io.github.pureza.happynews.storage;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.user.Editor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ArticleRepositoryTest extends AbstractTest {

    private ArticleRepository repository;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        repository = new ArticleRepository(config);
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void loadArticlesCreatesArticlesHomeIfItDoesNotExist() throws IOException {
        Files.delete(config.articlesHome());
        assertThat(Files.exists(config.articlesHome()), equalTo(false));

        repository.loadArticles();
        assertThat(Files.exists(config.articlesHome()), equalTo(true));
    }


    @Test
    public void loadArticlesLoadsNoArticlesWhenThereAreNone() throws IOException {
        assertThat(repository.loadArticles(), equalTo(Collections.emptyMap()));
    }


    @Test
    public void loadArticlesLoadsExistingArticles() throws IOException {
        Files.createFile(config.articlesHome().resolve("1@host"));
        Files.createFile(config.articlesHome().resolve("2@host"));

        Map<String, Article> expected = new HashMap<>();
        expected.put("<1@host>", new Article("<1@host>", config.articlesHome()));
        expected.put("<2@host>", new Article("<2@host>", config.articlesHome()));
        assertThat(repository.loadArticles(), equalTo(expected));
    }


    @Test
    public void addFailsIfHeaderDoesntContainNewsgroupsField() throws IOException {
        Editor user = mockEditor("user");
        ArticleHeader header = new ArticleHeader("");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        assertThat(repository.add(header, body, user), equalTo(false));
    }


    @Test
    public void addFailsIfHeaderDoesntContainFromField() throws IOException {
        Editor user = mockEditor("user");
        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        assertThat(repository.add(header, body, user), equalTo(false));
    }


    @Test
    public void addFailsIfHeaderDoesntContainSubjectField() throws IOException {
        Editor user = mockEditor("user");
        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<user@host.org>");
        String body = "A perfect body";

        assertThat(repository.add(header, body, user), equalTo(false));
    }


    @Test
    public void addFailsIfFromFieldIsInvalid() throws IOException {
        Editor user = mockEditor("user");
        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "illegal_from");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        assertThat(repository.add(header, body, user), equalTo(false));
    }


    @Test
    public void addCreatesFileWithIncrementalNumberAndSenderHost() throws IOException {
        Editor user = mockEditor("user");

        {
            ArticleHeader header = new ArticleHeader("");
            header.put("Newsgroups", "happynews.users");
            header.put("From", "<user@host.org>");
            header.put("Subject", "Hello, world");
            String body = "A perfect body";
            assertThat(repository.add(header, body, user), equalTo(true));
            assertThat(Files.exists(config.articlesHome().resolve("1@host.org")), equalTo(true));
        }

        // Add another article
        {
            ArticleHeader header = new ArticleHeader("");
            header.put("Newsgroups", "happynews.users");
            header.put("From", "<user@example.org>");
            header.put("Subject", "Goodbye!");
            String body = "Body building";
            assertThat(repository.add(header, body, user), equalTo(true));
            assertThat(Files.exists(config.articlesHome().resolve("2@example.org")), equalTo(true));
        }
    }


    @Test
    public void addAppendsMessageIdFieldToHeader() throws IOException {
        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        assertThat(repository.add(header, body, user), equalTo(true));
        assertThat(header.get("Message-ID"), equalTo("<1@host.org>"));
    }


    @Test
    public void addCreatesEmptyFileInsideUserHome() throws IOException {
        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        // before...
        assertThat(Files.exists(user.getHome().resolve("1@host.org")), equalTo(false));

        assertThat(repository.add(header, body, user), equalTo(true));

        // ... after
        assertThat(Files.exists(user.getHome().resolve("1@host.org")), equalTo(true));
    }


    @Test
    public void addAppendsArticleToArticlesMap() throws IOException {
        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        // before...
        assertThat(repository.get("<1@host.org>"), is(nullValue()));

        assertThat(repository.add(header, body, user), equalTo(true));

        // ... after
        assertThat(repository.get("<1@host.org>"), equalTo(new Article("<1@host.org>", config.articlesHome())));
    }


    @Test
    public void addWritesTheRightContent() throws IOException {
        Editor user = mockEditor("user");

        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");
        String body = "A perfect body";

        assertThat(repository.add(header, body, user), equalTo(true));

        assertThat(Files.readAllLines(config.articlesHome().resolve("1@host.org")), equalTo(
                asList("newsgroups: happynews.users",
                        "from: <user@host.org>",
                        "subject: Hello, world",
                        "message-id: <1@host.org>",
                        "",
                        "A perfect body")));
    }
}

package io.github.pureza.happynews;

import io.github.pureza.happynews.config.Config;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.Reader;
import io.github.pureza.happynews.user.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class AbstractTest {

    protected NNTPServer server;

    protected Path baseDir;

    protected Config config;


    protected void setUp() throws Exception {
        // Create the test base dir
        baseDir = Files.createTempDirectory("happynews_tests");

        // Test configuration
        config = new Config() {
            @Override
            public Path baseDir() {
                return baseDir;
            }

            @Override
            public Path usersHome() {
                return baseDir.resolve("users");
            }

            @Override
            public Path articlesHome() {
                return baseDir.resolve("articles");
            }

            @Override
            public Path groupsFile() {
                return baseDir.resolve("groups.tsv");
            }

            @Override
            public Path usersFile() {
                return baseDir.resolve("users.tsv");
            }
        };

        // Create users/
        Files.createDirectory(config.usersHome());

        // Create articles/
        Files.createDirectory(config.articlesHome());


        server = mock(NNTPServer.class);
        when (server.config()).thenReturn(config);
    }


    protected void tearDown() {
        try {
            // Delete all files within the test base dir
            Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            // Do nothing
        }
    }


    /**
     * Creates a mock user with role Reader
     */
    public Reader mockReader(String username) {
        Reader reader = new Reader(username, username);
        reader.setSalt("12345678");
        return reader;
    }


    /**
     * Creates a mock user with role Editor
     */
    public Editor mockEditor(String username) throws IOException {
        Path userHome = config.usersHome().resolve(username);
        if (!Files.exists(userHome)) {
            Files.createDirectory(userHome);
        }

        Editor editor = new Editor(username, username, userHome);
        editor.setSalt("12345678");
        return editor;
    }


    /**
     * Creates a mock user with role Admin
     */
    public Admin mockAdmin(String username) throws IOException {
        Path userHome = config.usersHome().resolve(username);
        Files.createDirectory(userHome);
        Admin admin = new Admin(username, username, userHome);
        admin.setSalt("12345678");
        return admin;
    }


    /**
     * Mocks the user's input and returns a Future that will contain the output
     * that is sent to the user
     */
    public Supplier<String> mockInput(User user, String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);

        Socket mockSocket = mock(Socket.class);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes()));
        when(mockSocket.getOutputStream()).thenReturn(out);
        when(mockSocket.getInetAddress()).thenReturn(InetAddress.getLoopbackAddress());
        user.setClientSocket(mockSocket);

        return () -> {
            try {
                return out.toString(Charset.defaultCharset().name());
            } catch (UnsupportedEncodingException e) {
                // Won't happen
                return null;
            }
        };
    }


    /**
     * Creates a dummy article file
     */
    public Article mockArticle(String fileName, Editor editor, String subject, String newsgroups) throws IOException {
        String articleId = "<" + fileName + ">";
        Files.createFile(editor.getPath().resolve(fileName));

        SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM yy HH:mm:ss");
        Files.write(config.articlesHome().resolve(fileName), asList(
                "From: " + editor.getUsername() + "@example.org",
                "Subject: " + subject,
                "Newsgroups: " + newsgroups,
                "Message-ID: " + articleId,
                "Date: $date",
                "",
                "Article from " + editor.getUsername()), StandardOpenOption.CREATE);

        return new Article(articleId, config.articlesHome());
    }


    /**
     * Creates a dummy article file with an empty body
     */
    public Article mockArticleWithEmptyBody(String fileName, Editor editor, String subject, String newsgroups) throws IOException {
        String articleId = "<" + fileName + ">";
        Files.createFile(editor.getPath().resolve(fileName));

        SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM yy HH:mm:ss");
        Files.write(config.articlesHome().resolve(fileName), asList(
                "From: " + editor.getUsername() + "@example.org",
                "Subject: " + subject,
                "Newsgroups: " + newsgroups,
                "Message-ID: " + articleId,
                "Date: $date",
                ""), StandardOpenOption.CREATE);

        return new Article(articleId, config.articlesHome());
    }



    /**
     * Creates a dummy newsgroup
     */
    public Newsgroup mockNewsgroup(String name, String... articleIds) {
        return new Newsgroup(name, new Date(), asList(articleIds));
    }
}

package io.github.pureza.happynews.storage;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.Reader;
import io.github.pureza.happynews.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static io.github.pureza.happynews.Tests.date;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

public class UserRepositoryTest extends AbstractTest {

    private UserRepository repository;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.repository = new UserRepository(config, emptyList());
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void loadUsersReturnsEmptyIfFileDoesNotExist() throws Exception {
        assertThat(repository.loadUsers(), is(emptyList()));
    }


    @Test
    public void loadUsersLoadsUsers() throws Exception {
        User reader = mockReader("reader");
        User admin = mockAdmin("admin");
        List<User> users = asList(reader, admin);

        Files.write(config.usersFile(), asList(
                String.format("%s\t%s\t%s\t%s", reader.getUsername(), reader.getPassword(), reader.getSalt(), reader.getRole().name()),
                String.format("%s\t%s\t%s\t%s", admin.getUsername(), admin.getPassword(), admin.getSalt(), admin.getRole().name())));

        assertThat(repository.loadUsers(), equalTo(users));
    }


    @Test(expected=Exception.class)
    public void loadUsersFailsOnIllegalUsers() throws Exception {
        Files.write(config.usersFile(), asList(
                "reader\t12345678\tsalt\treader",
                "admin\tpassword\tsalt\tINVALID_ROLE"));

        repository.loadUsers();
    }


    @Test
    public void loadUsersCreatesUsersHomeIfItDoesntExist() throws Exception {
        Files.delete(config.usersHome());

        // No users
        Files.createFile(config.usersFile());

        repository.loadUsers();

        assertThat(Files.exists(config.usersHome()), is(true));
    }


    @Test
    public void loadUsersDoesNotTouchUsersHomeIfIExists() throws Exception {
        assertThat(Files.exists(config.usersHome()), is(true));

        // No users
        Files.createFile(config.usersFile());
        repository.loadUsers();

        assertThat(Files.exists(config.usersHome()), is(true));
    }

    @Test
    public void addValidatesUsername() {
        assertThat(repository.add(new Reader("invalid username", "password")), equalTo(false));
    }


    @Test
    public void addValidatesPassword() {
        assertThat(repository.add(new Reader("reader", "pass word")), equalTo(false));
    }


    @Test
    public void addChecksForDuplicates() {
        assertThat(repository.add(new Reader("reader", "password")), equalTo(true));
        assertThat(repository.add(new Reader("reader", "other")), equalTo(false));
    }


    @Test
    public void addAddsUser() {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), equalTo(true));
        assertThat(repository.get(reader.getUsername()), equalTo(reader));
    }


    @Test
    public void addHashesPassword() {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), equalTo(true));
        assertThat(repository.get(reader.getUsername()), equalTo(reader));
        assertThat(reader.getPassword(), is(not("password")));
        assertThat(reader.getSalt(), is(not(nullValue())));
    }


    @Test
    public void addEditorCreatesEditorHome() {
        Editor editor = new Editor("editor", "password", config.usersHome());
        assertThat(repository.add(editor), equalTo(true));
        assertThat(Files.exists(editor.getHome()), is(true));
    }


    @Test
    public void addWritesUserToFile() throws Exception {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), equalTo(true));
        assertThat(repository.loadUsers(), is(singletonList(reader)));
    }


    @Test
    public void removeIgnoresNonexistentUser() {
        assertThat(repository.remove("nonexistent"), is(false));
    }


    @Test
    public void removeIgnoresOnlineUser() {
        User reader = new Reader("reader", "password");

        assertThat(repository.add(reader), is(true));

        reader.setClientSocket(mock(Socket.class));
        assertThat(reader.isOnline(), is(true));

        assertThat(repository.remove(reader.getUsername()), is(false));
    }


    @Test
    public void removeRemovesUser() {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), is(true));
        assertThat(repository.get(reader.getUsername()), is(reader));
        assertThat(repository.remove(reader.getUsername()), is(true));
        assertThat(repository.users(), is(emptyMap()));
    }


    @Test
    public void removeWritesToUserFile() throws Exception {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), equalTo(true));
        assertThat(repository.remove(reader.getUsername()), is(true));
        assertThat(repository.loadUsers(), is(emptyList()));
    }


    @Test
    public void changePasswordValidatesPassword() {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), equalTo(true));
        assertThat(repository.changeUserPassword(reader.getUsername(), "invalid password"), is(false));
        assertThat(repository.get(reader.getUsername()).getPassword(), is(reader.getPassword()));
    }


    @Test
    public void changePasswordFailsForNonexistentUser() {
        assertThat(repository.changeUserPassword("non_existent_user", "password"), is(false));
    }


    @Test
    public void changePasswordHashesNewPassword() {
        User reader = new Reader("reader", "password");
        String originalPassword = reader.getPassword();
        String originalSalt = reader.getSalt();
        assertThat(repository.add(reader), equalTo(true));
        assertThat(repository.changeUserPassword(reader.getUsername(), "new-password"), is(true));
        assertThat(repository.get(reader.getUsername()).getPassword(), is(not(originalPassword)));
        assertThat(repository.get(reader.getUsername()).getSalt(), is(not(originalSalt)));
        assertThat(repository.authenticate(reader.getUsername(), "new-password"), is(true));
    }


    @Test
    public void changePasswordWritesToUserFile() throws Exception {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), equalTo(true));
        assertThat(repository.changeUserPassword(reader.getUsername(), "new-password"), is(true));

        User reader2 = repository.get(reader.getUsername());
        assertThat(repository.loadUsers(), is(singletonList(reader2)));
    }


    @Test
    public void changeRoleFailsIfUserDoesNotExist() {
        assertThat(repository.changeUserRole("non_existent_user", User.Role.ADMIN), is(false));
    }


    @Test
    public void changeRoleFailsIfUserIsOnline() {
        User reader = new Reader("reader", "password");

        assertThat(repository.add(reader), is(true));

        reader.setClientSocket(mock(Socket.class));
        assertThat(reader.isOnline(), is(true));

        assertThat(repository.changeUserRole(reader.getUsername(), User.Role.EDITOR), is(false));
    }


    @Test
    public void changeRoleChangesRole() {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), is(true));
        assertThat(repository.changeUserRole(reader.getUsername(), User.Role.EDITOR), is(true));
        assertThat(repository.get(reader.getUsername()).getRole(), is(User.Role.EDITOR));
    }


    @Test
    public void changeRoleFailsOneTheSameRole() {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), is(true));
        assertThat(repository.changeUserRole(reader.getUsername(), User.Role.READER), is(false));
    }


    @Test
    public void changeRoleWritesToUserFile() throws Exception {
        User reader = new Reader("reader", "password");
        assertThat(repository.add(reader), is(true));
        assertThat(repository.changeUserRole(reader.getUsername(), User.Role.EDITOR), is(true));

        Editor editor = (Editor) repository.get(reader.getUsername());
        assertThat(repository.loadUsers(), is(singletonList(editor)));
    }


    @Test
    public void writeUsersWritesEmptyFileWhenThereAreNoUsers() throws Exception {
        repository.writeUsers();
        assertThat(repository.loadUsers(), is(emptyList()));
    }


    @Test
    public void writeUsersWritesUsers() throws Exception {
        User reader = mockReader("reader");
        User admin = mockAdmin("admin");
        List<User> users = asList(reader, admin);

        UserRepository repository = new UserRepository(config, users);
        repository.writeUsers();
        assertThat(new HashSet<>(repository.loadUsers()), is(new HashSet<>(users)));
    }


    @Test
    public void authenticateFailsForUnknownUser() throws Exception {
        assertThat(repository.authenticate("nonexistent_user", "password"), is(false));
    }


    @Test
    public void authenticateFailsForWrongPassword() throws Exception {
        User user = mockReader("reader");
        repository.add(user);

        assertThat(repository.authenticate(user.getUsername(), user.getPassword() + "extra"), is(false));
    }


    @Test
    public void authenticateFailsIfUserIsAlreadyOnline() throws Exception {
        User user = mockReader("reader");
        String originalPassword = user.getPassword();
        repository.add(user);

        user.setClientSocket(mock(Socket.class));

        assertThat(user.isOnline(), is(true));
        assertThat(repository.authenticate(user.getUsername(), originalPassword), is(false));
    }


    @Test
    public void authenticateSucceedsForCorrectPassword() throws Exception {
        User user = mockReader("reader");
        String originalPassword = user.getPassword();
        repository.add(user);

        assertThat(repository.authenticate(user.getUsername(), originalPassword), is(true));
    }
}

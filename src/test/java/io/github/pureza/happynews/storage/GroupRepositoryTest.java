package io.github.pureza.happynews.storage;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.user.Editor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.acl.Group;
import java.util.*;

import static io.github.pureza.happynews.Tests.date;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.contains;

public class GroupRepositoryTest extends AbstractTest {

    private GroupRepository repository;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.repository = new GroupRepository(config, emptyList());
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void loadGroupsReturnsEmptyListIfFileDoesNotExist() throws IOException {
        assertThat(repository.loadGroups(), equalTo(emptyList()));
    }


    @Test
    public void loadGroupsLoadsGroups() throws IOException {
        Newsgroup group1 = new Newsgroup("group1", new Date(), emptyList());
        Newsgroup group2 = new Newsgroup("group2", date(2001, 12, 1, 12, 30), asList("<1@host.com>", "<2@example.org>"));
        List<Newsgroup> groups = asList(group1, group2);

        Files.write(config.groupsFile(), asList(
                String.format("%s\t%d", group1.getName(), group1.getDateCreated().getTime()),
                String.format("%s\t%d\t%s", group2.getName(), group2.getDateCreated().getTime(), group2.articles().stream().collect(joining(",")))));

        assertThat(repository.loadGroups(), equalTo(groups));
    }


    @Test(expected=Exception.class)
    public void loadGroupsFailsOnIllegalGroups() throws IOException {
        Files.write(config.groupsFile(), asList(
                String.format("group1\t12345678"),
                String.format("group2\tTHIS_WILL_THROW_AN_ERROR")));

        repository.loadGroups();
    }


    @Test
    public void addConvertsNameToLowerCase() {
        assertThat(repository.add("FUNNY.GROUP"), equalTo(true));
        assertThat(repository.get("funny.group").getName(), equalTo("funny.group"));
    }


    @Test
    public void addValidatesGroupName() {
        assertThat(repository.add("inval<id@name"), equalTo(false));
    }


    @Test
    public void addChecksForDuplicates() {
        assertThat(repository.add("group"), equalTo(true));
        assertThat(repository.add("group"), equalTo(false));
    }


    @Test
    public void addCreatesGroup() {
        Date before = new Date();

        assertThat(repository.add("funny.group"), equalTo(true));
        Date created = repository.get("funny.group").getDateCreated();

        Date after = new Date();

        assertThat(created.equals(before) || created.after(before), is(true));
        assertThat(created.equals(after) || created.before(after), is(true));

        assertThat(repository.get("funny.group").articles(), equalTo(emptyList()));
    }


    @Test
    public void writeGroupsWritesEmptyFileWhenThereAreNoGroups() {
        repository.writeGroups();
        assertThat(repository.loadGroups(), is(emptyList()));
    }


    @Test
    public void writeGroupsWritesGroups() {
        Newsgroup group1 = new Newsgroup("group1", new Date(), emptyList());
        Newsgroup group2 = new Newsgroup("group2", date(2001, 12, 1, 12, 30), asList("<1@host.com>", "<2@example.org>"));
        List<Newsgroup> groups = asList(group1, group2);

        GroupRepository repository = new GroupRepository(config, groups);
        repository.writeGroups();
        assertThat(new HashSet<>(repository.loadGroups()), is(new HashSet<>(groups)));
    }
}

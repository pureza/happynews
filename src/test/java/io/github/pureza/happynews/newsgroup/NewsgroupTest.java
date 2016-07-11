package io.github.pureza.happynews.newsgroup;

import org.junit.Test;

import java.util.Date;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NewsgroupTest {

    @Test(expected=IndexOutOfBoundsException.class)
    public void getArticleIdFailsAtZero() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        group.getArticleId(0);
    }


    @Test(expected=IndexOutOfBoundsException.class)
    public void getArticleIdFailsForLargeIndex() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        group.getArticleId(3);
    }


    @Test
    public void getArticleIdStartsAtOne() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        assertThat(group.getArticleId(1), is("<1@host.com>"));
    }


    @Test(expected=NoSuchElementException.class)
    public void nextIndexFailsWhenThereIsNoNext() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        group.nextIndex(2);
    }


    @Test
    public void nextIndexReturnsTheIndexOfTheFollowingArticle() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        assertThat(group.nextIndex(1), is(2));
    }


    @Test
    public void nextIndexSucceedsForZero() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        assertThat(group.nextIndex(0), is(1));
    }


    @Test(expected=NoSuchElementException.class)
    public void previousIndexFailsWhenThereIsNoPrevious() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        group.previousIndex(1);
    }


    @Test
    public void previousIndexReturnsTheIndexOfThePreviousArticle() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        assertThat(group.previousIndex(2), is(1));
    }


    @Test
    public void nextIndexSucceedsForLargeIndex() {
        Newsgroup group = new Newsgroup("group", new Date(), asList("<1@host.com>", "<2@host.com>"));
        assertThat(group.previousIndex(3), is(2));
    }
}

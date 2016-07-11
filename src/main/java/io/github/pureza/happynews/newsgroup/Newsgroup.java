package io.github.pureza.happynews.newsgroup;

import java.util.*;
import java.io.*;

/**
 * A newsgroup
 *
 * This class is thread safe.
 */
public class Newsgroup implements Serializable {

    /** Newsgroup name */
    private final String name;

    /** Date of creation */
    private final Date dateCreated;

    /**
     * Newsgroup articles
     * This is a shared resource, so all accesses need to be synchronized
     */
    private final List<String> articles = new ArrayList<>();


    public Newsgroup(String name) {
        this.name = name;
        this.dateCreated = new Date();
    }


    public Newsgroup(String name, Date dateCreated, List<String> articles) {
        this.name = name;
        this.dateCreated = dateCreated;
        this.articles.addAll(articles);
    }


    /**
     * Returns the newsgroup articles
     */
    public List<String> articles() {
        synchronized (articles) {
            return articles;
        }
    }


    /**
     * Adds a new article to the newsgroup
     */
    public void addArticle(String id) {
        synchronized (articles) {
            articles.add(id);
        }
    }


    /**
     * Checks if there are articles in this newsgroup
     */
    public boolean isEmpty() {
        synchronized (articles) {
            return articles.isEmpty();
        }
    }


    /**
     * Retrieves the id of the article at the given index
     */
    public String getArticleId(int n) {
        synchronized (articles) {
            return articles.get(n - 1);
        }
    }


    /**
     * Returns the newsgroup name
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the date of creation of this newsgroup
     */
    public Date getDateCreated() {
        return dateCreated;
    }


    /**
     * Returns the number of the first article in this newsgroup
     */
    public int getFirstArticleNum() {
        return 1;
    }


    /**
     * Returns the number of the last article in this newsgroup
     */
    public int getLastArticleNum() {
        synchronized (articles) {
            return articles.size();
        }
    }


    /**
     * Checks if there is an article after the given one
     */
    public boolean hasNext(int curIndex) {
        return containsArticleNum(curIndex + 1);
    }


    /**
     * Returns the index of the article that follows the one at the given index
     *
     * @throws NoSuchElementException if there is no article at the given index
     */
    public int nextIndex(int curIndex) {
        if (!hasNext(curIndex)) {
            throw new NoSuchElementException();
        }

        return curIndex + 1;
    }


    /**
     * Checks if there is an article before the given one
     */
    public boolean hasPrevious(int curIndex) {
        return containsArticleNum(curIndex - 1);
    }


    /**
     * Returns the index of the article that precedes the one at the given index
     *
     * @throws NoSuchElementException if there is no article at the given index
     */
    public int previousIndex(int curIndex) {
        if (!hasPrevious(curIndex)) {
            throw new NoSuchElementException();
        }

        return curIndex - 1;
    }


    /**
     * Checks if there is an article at the given index
     */
    public boolean containsArticleNum(int num) {
        return num > 0 && num <= getLastArticleNum();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Newsgroup newsgroup = (Newsgroup) o;
        return Objects.equals(name, newsgroup.name) &&
                Objects.equals(dateCreated, newsgroup.dateCreated) &&
                Objects.equals(articles, newsgroup.articles);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, dateCreated, articles);
    }


    @Override
    public String toString() {
        return "Newsgroup{" +
                "dateCreated=" + dateCreated +
                ", articles=" + articles +
                ", name='" + name + '\'' +
                '}';
    }
}

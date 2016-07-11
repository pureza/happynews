package io.github.pureza.happynews.user;

import io.github.pureza.happynews.newsgroup.Newsgroup;

import java.net.Socket;
import java.util.Objects;

/**
 * A user
 */
public abstract class User {

    /**
     * User role
     */
    public enum Role { ADMIN, EDITOR, READER }

    /** Username */
    protected String username;

    /** User password */
    protected String password;

    /** Password salt */
    protected String salt;

    /** The group the user is currently reading */
    protected Newsgroup currentGroup;

    /** Index of the article the user is reading */
    protected int articleIndex = 0;

    /** Client socket */
    protected Socket clientSocket;


    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }


    public String getSalt() {
        return salt;
    }


    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public abstract Role getRole();


    public Newsgroup getCurrentGroup() {
        return currentGroup;
    }


    public void setCurrentGroup(Newsgroup currentGroup) {
        this.currentGroup = currentGroup;
        articleIndex = currentGroup.getFirstArticleNum();
    }


    public int getCurrentArticleIndex() {
        return articleIndex;
    }


    public void setCurrentArticleIndex(int articleIndex) {
        this.articleIndex = articleIndex;
    }


    public String getCurrentArticleId() {
        return currentGroup.getArticleId(articleIndex);
    }


    public void setClientSocket(Socket s) {
        clientSocket = s;
    }


    public boolean isOnline() {
        return clientSocket != null && !clientSocket.isClosed();
    }


    public Socket getClientSocket() {
        return clientSocket;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return articleIndex == user.articleIndex &&
                Objects.equals(username, user.username) &&
                Objects.equals(password, user.password) &&
                Objects.equals(salt, user.salt) &&
                Objects.equals(currentGroup, user.currentGroup) &&
                Objects.equals(clientSocket, user.clientSocket);
    }


    @Override
    public int hashCode() {
        return Objects.hash(username, password, salt, currentGroup, articleIndex, clientSocket);
    }


    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", currentGroup=" + currentGroup +
                ", articleIndex=" + articleIndex +
                ", clientSocket=" + clientSocket +
                '}';
    }
}

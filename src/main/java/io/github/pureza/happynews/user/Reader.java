package io.github.pureza.happynews.user;

/**
 * A reader
 */
public class Reader extends User {
    public Reader(String username, String password) {
        this.username = username;
        this.password = password;
    }


    @Override
    public Role getRole() {
        return Role.READER;
    }
}

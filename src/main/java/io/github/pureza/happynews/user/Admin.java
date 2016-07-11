package io.github.pureza.happynews.user;

import java.nio.file.Path;

/**
 * An administrator
 *
 * Administrators are also editors.
 */
public class Admin extends Editor {

    public Admin(String username, String password, Path home) {
        super(username, password, home);
    }


    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}

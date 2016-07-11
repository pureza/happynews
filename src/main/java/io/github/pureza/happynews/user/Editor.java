package io.github.pureza.happynews.user;

import java.nio.file.Path;

/**
 * An editor
 *
 * Editors have a special "home" directory where they can store documents
 */
public class Editor extends Reader {

    /** User's home directory */
    private final Path home;

    /** Current working directory */
    private Path path;


    public Editor(String username, String password, Path home) {
        super(username, password);
        this.home = home;
        this.path = home;
    }


    public Path getPath() {
        return path;
    }


    public Path getHome() {
        return home;
    }


    public void setPath(Path newPath) {
        this.path = newPath;
    }


    @Override
    public Role getRole() {
        return Role.EDITOR;
    }
}

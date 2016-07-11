package io.github.pureza.happynews.config;


import java.nio.file.Path;

/**
 * Application configuration
 */
public abstract class Config {

    /**
     * Base path where all files are stored
     */
    public abstract Path baseDir();


    /**
     * Path to the users home directory, relative to the base dir
     */
    public abstract Path usersHome();


    /**
     * Path to the articles home directory, relative to the base dir
     */
    public abstract Path articlesHome();


    /**
     * Path to the file containing the groups, relative to the base dir
     */
    public abstract Path groupsFile();


    /**
     * Path to the file containing the user details, relative to the base dir
     */
    public abstract Path usersFile();
}

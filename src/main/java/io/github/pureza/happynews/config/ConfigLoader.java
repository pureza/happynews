package io.github.pureza.happynews.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration loader
 */
public class ConfigLoader {

    /**
     * Loads the configuration from the application.properties file
     */
    public static Config load() throws IOException {
        Properties props = new Properties();
        props.load(ConfigLoader.class.getResourceAsStream("/application.properties"));

        return new Config() {
            @Override
            public Path baseDir() {
                return Paths.get(props.getProperty("base.dir"));
            }

            @Override
            public Path usersHome() {
                return baseDir().resolve(props.getProperty("users.dir")).normalize();
            }

            @Override
            public Path articlesHome() {
                return baseDir().resolve(props.getProperty("articles.dir")).normalize();
            }

            @Override
            public Path groupsFile() {
                return baseDir().resolve(props.getProperty("groups.file")).normalize();
            }

            @Override
            public Path usersFile() {
                return baseDir().resolve(props.getProperty("users.file")).normalize();
            }
        };
    }
}

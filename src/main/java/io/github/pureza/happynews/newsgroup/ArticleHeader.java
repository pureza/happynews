package io.github.pureza.happynews.newsgroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Article header
 */
public class ArticleHeader {

    /** The full header text */
    private final StringBuilder headerText = new StringBuilder();

    /** Header fields */
    private final Map<String, String> fields = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a new ArticleHeader from a file containing the article contents
     */
    public ArticleHeader(File article) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(article))) {
            readHeader(in);
        }
    }


    /**
     * Create a new ArticleHeader from a string
     */
    public ArticleHeader(String text) {
        try {
            readHeader(new BufferedReader(new StringReader(text)));
        } catch (IOException ex) {
            // This should not happen because it's not reading from a file
            logger.error("An error occurred while parsing the header", ex);
        }
    }


    /**
     * Adds a new field to the header.
     *
     * If the field already existed, replaces its value and returns the old value.
     */
    public String put(String field, String value) {
        field = field.toLowerCase();

        // New field: just add it
        if (!fields.containsKey(field)) {
            if (!fields.isEmpty()) {
                headerText.append("\n");
            }
            headerText.append(field).append(": ").append(value);
            return fields.put(field, value);
        } else {
            // Existing field: need to rebuild the headerText
            String old = fields.put(field, value);
            headerText.setLength(0);

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                headerText.append(entry.getKey()).append(": ").append(entry.getValue());
                headerText.append("\n");
            }
            headerText.deleteCharAt(headerText.length() - 1);
            return old;
        }
    }


    /**
     * Returns the value of the given field
     */
    public String get(String field) {
        return fields.get(field.toLowerCase());
    }


    /**
     * Checks if the header contains the given field
     */
    public boolean contains(String field) {
        return fields.containsKey(field.toLowerCase());
    }


    @Override
    public String toString() {
        return headerText.toString();
    }


    /**
     * Reads the header from the given input stream
     */
    private void readHeader(BufferedReader in) throws IOException {
        String line;
        String previousField = null;
        while ((line = in.readLine()) != null && line.length() > 0) {
            if (line.startsWith(" ")) {
                if (previousField == null) {
                    throw new IllegalArgumentException("header is not correctly formatted in line " + line);
                }

                // Append to the previous field
                put(previousField, get(previousField) + "\n" + line);
            } else {
                if (!line.contains(":")) {
                    throw new IllegalArgumentException("header is not correctly formatted in line " + line);
                }

                String field = line.split(":")[0].trim();
                String value = line.substring(line.indexOf(":") + 1, line.length()).trim();

                put(field, value);

                previousField = field;
            }
        }
    }
}

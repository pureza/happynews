package io.github.pureza.happynews.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

public class StringsTest {

    @Test
    public void toSentenceCaseReturnsNullOnNull() {
        assertThat(Strings.toSentenceCase(null), is(nullValue()));
    }


    @Test
    public void toSentenceCaseReturnsEmptyOnEmpty() {
        assertThat(Strings.toSentenceCase(""), is(""));
    }


    @Test
    public void toSentenceCaseHasNoEffectInWhitespace() {
        assertThat(Strings.toSentenceCase(" HELLO"), is(" hello"));
    }


    @Test
    public void toSentenceCapitalizesSingleLetter() {
        assertThat(Strings.toSentenceCase("h"), is("H"));
    }


    @Test
    public void toSentenceCaseConvertsTextToSentenceCase() {
        assertThat(Strings.toSentenceCase("hello"), is("Hello"));
        assertThat(Strings.toSentenceCase("HELLO"), is("Hello"));
        assertThat(Strings.toSentenceCase("Hello World"), is("Hello world"));
    }
}

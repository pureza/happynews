package io.github.pureza.happynews.validation;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NewsgroupValidatorTest {

    @Test
    public void isValidNewsgroupNameAcceptsValidNames() {
        assertThat(new NewsgroupValidator().isValidNewsgroupName("allLetters"), is(true));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("lettersAndD1g1ts"), is(true));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("letters.dots"), is(true));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("letters_underscores123"), is(true));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("_"), is(true));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("_123"), is(true));
    }


    @Test
    public void isValidNewsgroupNameFailsOnInvalidNames() {
        assertThat(new NewsgroupValidator().isValidNewsgroupName(""), is(false));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("1"), is(false));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("."), is(false));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("hello."), is(false));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("he..loo"), is(false));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("hell@123"), is(false));
        assertThat(new NewsgroupValidator().isValidNewsgroupName("123hello"), is(false));
    }


    @Test
    public void isValidListOfNewsgroupNamesAcceptsSingleName() {
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames("happynews.users"), is(true));
    }


    @Test
    public void isValidListOfNewsgroupNamesAcceptsValidList() {
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames("happynews.users,hello123,another_one"), is(true));
    }


    @Test
    public void isValidListOfNewsgroupNamesAcceptsValidListWithSpaces() {
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames("happynews.users, hello123, another_one"), is(true));
    }


    @Test
    public void isValidListOfNewsgroupNamesReffusesInvalidList() {
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames(""), is(false));
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames(","), is(false));
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames(",,"), is(false));
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames(",hello"), is(false));
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames("hello,"), is(false));
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames("hello,,another"), is(false));
        assertThat(new NewsgroupValidator().isValidListOfNewsgroupNames("hello,123"), is(false));
    }
}

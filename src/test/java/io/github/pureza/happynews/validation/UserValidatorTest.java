package io.github.pureza.happynews.validation;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UserValidatorTest {

    @Test
    public void isValidUsernameAcceptsValidUsernames() {
        assertThat(new UserValidator().isValidUsername("allLetters"), is(true));
        assertThat(new UserValidator().isValidUsername("lettersAndD1g1ts"), is(true));
        assertThat(new UserValidator().isValidUsername("letters.dots"), is(true));
        assertThat(new UserValidator().isValidUsername("letters_underscores123"), is(true));
        assertThat(new UserValidator().isValidUsername("_"), is(true));
        assertThat(new UserValidator().isValidUsername("_123"), is(true));
    }


    @Test
    public void isValidUsernameFailsOnInvalidUsernames() {
        assertThat(new UserValidator().isValidUsername(""), is(false));
        assertThat(new UserValidator().isValidUsername("1"), is(false));
        assertThat(new UserValidator().isValidUsername("."), is(false));
        assertThat(new UserValidator().isValidUsername("hello."), is(false));
        assertThat(new UserValidator().isValidUsername("he..loo"), is(false));
        assertThat(new UserValidator().isValidUsername("hell@123"), is(false));
        assertThat(new UserValidator().isValidUsername("123hello"), is(false));
    }


    @Test
    public void isValidPasswordAcceptsValidPasswords() {
        assertThat(new UserValidator().isValidPassword("allLetters"), is(true));
        assertThat(new UserValidator().isValidPassword("lettersAndD1g1ts"), is(true));
        assertThat(new UserValidator().isValidPassword("letters.dots"), is(true));
        assertThat(new UserValidator().isValidPassword("letters_underscores123"), is(true));
        assertThat(new UserValidator().isValidPassword("_@#$%ˆ}{}{{][][][][}\"\""), is(true));
    }


    @Test
    public void isValidPasswordFailsOnInvalidPasswords() {
        assertThat(new UserValidator().isValidPassword(""), is(false));
        assertThat(new UserValidator().isValidPassword(" "), is(false));
        assertThat(new UserValidator().isValidPassword("abc 123"), is(false));
        assertThat(new UserValidator().isValidPassword("abc\t123"), is(false));
        assertThat(new UserValidator().isValidPassword("abc "), is(false));
        assertThat(new UserValidator().isValidPassword(" abc"), is(false));
    }
}

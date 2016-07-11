package io.github.pureza.happynews.validation;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ValidatorsTest {

    @Test
    public void validateDotsAcceptsValidText() {
        assertThat(Validators.validateDots("abc"), is(true));
        assertThat(Validators.validateDots("abc.def.gh"), is(true));
    }


    @Test
    public void validateDotsReffusesInvalidTextWithDots() {
        assertThat(Validators.validateDots(""), is(false));
        assertThat(Validators.validateDots("."), is(false));
        assertThat(Validators.validateDots(".abc"), is(false));
        assertThat(Validators.validateDots("abc."), is(false));
        assertThat(Validators.validateDots("ab..c"), is(false));
    }
}

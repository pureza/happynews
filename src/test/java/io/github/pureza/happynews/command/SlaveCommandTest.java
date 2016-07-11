package io.github.pureza.happynews.command;

import io.github.pureza.happynews.AbstractTest;
import io.github.pureza.happynews.user.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class SlaveCommandTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void slaveIsNotImplemented() throws Exception {
        Reader user = mockReader("reader");
        Supplier<String> out = mockInput(user, "");
        new SlaveCommand(user, "SLAVE", server).process();

        assertThat(out.get(), containsString("202 slave status noted"));
    }
}

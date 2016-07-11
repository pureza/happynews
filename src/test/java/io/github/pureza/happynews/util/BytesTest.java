package io.github.pureza.happynews.util;

import org.junit.Test;

import static io.github.pureza.happynews.util.Bytes.fromHex;
import static io.github.pureza.happynews.util.Bytes.toHex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

public class BytesTest {

    @Test
    public void toHexConvertsByteArrayToHexadecimalString() {
        assertThat(toHex(new byte[] { 1, 2, 3, 10, (byte) 255 }), is("0102030aff"));
    }


    @Test
    public void fromHexConvertsHexadecimalStringToByteArray() {
        assertThat(fromHex("0102030aff"), is(new byte[] { 1, 2, 3, 10, (byte) 255 }));
    }


    @Test(expected=IllegalArgumentException.class)
    public void fromHexFailsIfTheStringDoesNotHaveEvenLength() {
        fromHex("0102030af");
    }


    @Test(expected=NumberFormatException.class)
    public void fromHexFailsIfTheStringIsInvalid() {
        fromHex("zz");
    }
}

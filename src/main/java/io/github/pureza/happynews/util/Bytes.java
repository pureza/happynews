package io.github.pureza.happynews.util;


/**
 * Utility methods related to byte arrays
 */
public class Bytes {

    private Bytes() {
        // This class can't be instantiated
    }


    /**
     * Converts an array of bytes to an hexadecimal number in string form
     */
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte byt : bytes) {
            sb.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }


    /**
     * Converts an hexadecimal number if string for to an array of bytes
     */
    public static byte[] fromHex(String hex) {
        if (!(hex.length() % 2 == 0)) {
            throw new IllegalArgumentException("Invalid hexadecimal string " + hex);
        }

        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            char first = hex.charAt(i * 2);
            char second = hex.charAt(i * 2 + 1);
            byte byt = (byte) ((Byte.valueOf(String.valueOf(first), 16) << 4) | Byte.valueOf(String.valueOf(second), 16));
            result[i] = byt;
        }

        return result;
    }
}

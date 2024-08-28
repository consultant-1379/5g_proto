package com.ericsson.sc.utils;

public class ConvUtils
{
    private ConvUtils()
    {

    }

    /**
     * Return next upper byte size that is dividable by 4 if it is not already.
     *
     * @param size
     * @return
     */
    public static int padTwo32(int size)
    {
        return size % 4 == 0 ? size : ((size / 4) + 1) * 4;
    }

    /**
     * Write short (2 bytes) in NW byte order (MSB left)
     *
     * @param value
     * @param b
     * @param offset
     * @return
     */
    public static byte[] toByteArrayLe(short value,
                                       byte[] b,
                                       int offset)
    {
        b[offset + 1] = (byte) (value >> 8);
        b[offset + 0] = (byte) (value >> 0);

        return b;
    }

    /**
     * Return new array in little-endian byte order
     *
     * @param value
     * @return
     */
    public static byte[] toByteArrayLe(short value)
    {
        var shortInNBO = new byte[2];
        return toByteArrayLe(value, shortInNBO, 0);
    }

    /**
     * Write int (4 bytes) in little-endian byte order
     *
     * @param value
     * @param b
     * @param offset
     * @return
     */
    public static byte[] toByteArrayLe(int value,
                                       byte[] b,
                                       int offset)
    {
        b[offset + 3] = (byte) (value >> 24);
        b[offset + 2] = (byte) (value >> 16);
        b[offset + 1] = (byte) (value >> 8);
        b[offset + 0] = (byte) (value >> 0);

        return b;
    }

    public static byte[] toByteArrayNbo(int value,
                                        byte[] b,
                                        int offset)
    {
        b[offset] = (byte) (value >> 24);
        b[offset + 1] = (byte) (value >> 16);
        b[offset + 2] = (byte) (value >> 8);
        b[offset + 3] = (byte) (value >> 0);

        return b;
    }

    /**
     * Return new array in little-endian byte order
     *
     * @param value
     * @return
     */
    public static byte[] toByteArrayLe(int value)
    {
        var intInLe = new byte[4];
        return toByteArrayLe(value, intInLe, 0);
    }

    /**
     * Write long (8 bytes) in little-endian byte order
     *
     * @param value
     * @param b
     * @param offset
     * @return
     */
    public static byte[] toByteArrayLe(long value,
                                       byte[] b,
                                       int offset)
    {
        b[offset + 7] = (byte) (value >> 56);
        b[offset + 6] = (byte) (value >> 48);
        b[offset + 5] = (byte) (value >> 40);
        b[offset + 4] = (byte) (value >> 32);
        b[offset + 3] = (byte) (value >> 24);
        b[offset + 2] = (byte) (value >> 16);
        b[offset + 1] = (byte) (value >> 8);
        b[offset + 0] = (byte) (value >> 0);

        return b;
    }

    /**
     * Return new array in NW byte order (MSB left)
     *
     * @param value
     * @return
     */
    public static byte[] toByteArrayLe(long value)
    {
        var longInLe = new byte[8];
        return toByteArrayLe(value, longInLe, 0);
    }

    public static byte[] pcapNgTimestampLe(long value)
    {
        var b = new byte[8];
        b[0] = (byte) (value >> 32);
        b[1] = (byte) (value >> 40);
        b[2] = (byte) (value >> 48);
        b[3] = (byte) (value >> 56);

        b[4] = (byte) (value >> 0);
        b[5] = (byte) (value >> 8);
        b[6] = (byte) (value >> 16);
        b[7] = (byte) (value >> 24);
        // 0 8 16 24 32 40 48 56

        return b;

    }

}

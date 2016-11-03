package cn.jony.libutil;

@SuppressWarnings("unused")
public class StringUtil {
    /**
     * count the bytes' num of String with utf-8 charset
     * @param s
     * @return
     */
    public static int getUtf8ByteLen(String s) {
        int size = s.length(), len = 0;
        for (int i = 0; i < size; i++) {
            len += getUtf8CodePointByteLen(s.codePointAt(i));
        }
        return len;
    }

    private static int getUtf8CodePointByteLen(int codePoint) {
        if (codePoint > 0x10ffff) {
            throw new IllegalArgumentException(
                    "Unexpected code point: " + Integer.toHexString(codePoint));
        }

        return codePoint < 0x80 ? 1 : (codePoint < 0x800 ? 2 : codePoint < 0x10000 ? 3 : 4);
    }
}

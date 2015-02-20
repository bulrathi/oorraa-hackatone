package ru.oorraa.common.eventbus;

import java.io.UnsupportedEncodingException;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
public final class SerializeUtil {

    private static final String DEFAULT_ENCODING = "UTF-8";

    public static byte[] string2Bytes(String str) throws UnsupportedEncodingException {
        return str.getBytes(DEFAULT_ENCODING);
    }

    public static String bytes2String(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, DEFAULT_ENCODING);
    }

}

package com.github.binpastes.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class IdGenerator {

    private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    public static String randomAlphaNumericalId() {
        return hex(sha1(UUID.randomUUID().toString().getBytes()));
    }

    protected static String hex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    protected static byte[] sha1(byte[] bytes) {
        try {
            final MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.update(bytes);
            return crypt.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private IdGenerator() {}

}

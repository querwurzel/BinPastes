package com.github.binpastes.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class IdGenerator {

    public static String randomAlphaNumericalId() {
        return randomAlphaNumericalId(UUID.randomUUID().toString().getBytes());
    }

    public static String randomAlphaNumericalId(byte[] bytes) {
        try {
            final MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.update(bytes);
            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private IdGenerator() {}

}

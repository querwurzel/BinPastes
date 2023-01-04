package com.github.binpastes.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public final class IdGenerator {

    public static String newStringId() {
        return DigestUtils.sha1Hex(UUID.randomUUID().toString());
    }

    private IdGenerator() {}

}

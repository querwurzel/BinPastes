package com.github.binpastes.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {

    @Test
    void sha1Hex() {
        var uuidv4 = UUID.fromString("9b1482c6-5310-4eef-b91d-75220eb2bef7");
        var id = IdGenerator.randomAlphaNumericalId(uuidv4.toString().getBytes());

        assertThat(id).isEqualTo("7870235b55ee013737d015c756c02f5efa6ba903");
    }
}

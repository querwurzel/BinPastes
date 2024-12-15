package com.github.binpastes.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static com.github.binpastes.util.IdGenerator.hex;
import static com.github.binpastes.util.IdGenerator.sha1;
import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {

    @Test
    void sha1Hex() {
        var uuidv4 = UUID.fromString("9b1482c6-5310-4eef-b91d-75220eb2bef7");
        var id = hex(sha1(uuidv4.toString().getBytes()));

        assertThat(id).hasSize(40);
        assertThat(id).isEqualTo("7870235b55ee013737d015c756c02f5efa6ba903");
    }

    @ParameterizedTest
    @MethodSource("ids")
    void massTest() {
        var id = IdGenerator.randomAlphaNumericalId();

        assertThat(id)
            .hasSize(40)
            .matches("[a-z0-9]{40}");
    }

    private static Stream<Arguments> ids() {
        return Stream.generate(IdGenerator::randomAlphaNumericalId)
            .limit(1000)
            .map(Arguments::arguments);
    }
}

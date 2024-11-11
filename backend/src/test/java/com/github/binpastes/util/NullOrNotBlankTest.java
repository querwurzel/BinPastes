package com.github.binpastes.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NullOrNotBlankTest {

    @ParameterizedTest
    @DisplayName("NullOrNotBlank validates correctly")
    @MethodSource("inputs")
    void nullOrNotBlank(String input, boolean isValid) {
        var validator = new NullOrNotBlank.NullOrNotBlankValidator();

        assertThat(validator.isValid(input, null)).isEqualTo(isValid);
    }

    private static Stream<Arguments> inputs() {
        return Stream.of(
                arguments(named("null", null), true),
                arguments(named("empty", ""), false),
                arguments(named("blank", " "), false),
                arguments(named("not blank", "abc"), true)
        );
    }

}
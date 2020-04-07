package com.tenchael.cords.common.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("hello", false),
                Arguments.of("", true),
                Arguments.of(null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void isBlank(String cs, boolean expect) {
        assertEquals(expect, StringUtils.isBlank(cs));
    }
}
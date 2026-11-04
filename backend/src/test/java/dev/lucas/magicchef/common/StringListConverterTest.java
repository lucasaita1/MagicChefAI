package dev.lucas.magicchef.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringListConverterTest {

    private final StringListConverter converter = new StringListConverter();

    @Test
    void roundTripsAsJsonArray() {
        String json = converter.convertToDatabaseColumn(List.of("2 ovos", "sal \"grosso\""));

        assertThat(json).isEqualTo("[\"2 ovos\",\"sal \\\"grosso\\\"\"]");
        assertThat(converter.convertToEntityAttribute(json)).containsExactly("2 ovos", "sal \"grosso\"");
    }

    @Test
    void handlesNullAndBlank() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("[]");
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute(" ")).isEmpty();
    }
}

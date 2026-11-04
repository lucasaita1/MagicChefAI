package dev.lucas.magicchef.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Stores a list of strings as a JSON array in a single column (portable between Postgres and H2).
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<String>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<String> values) {
        return MAPPER.writeValueAsString(values == null ? List.of() : values);
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return MAPPER.readValue(json, TYPE);
    }
}

package com.sample.sampleservice.shared.convertors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class StringListConverter implements AttributeConverter<Set<String>, String> {
    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(Set<String> stringList) {
        return stringList != null ? String.join(SPLIT_CHAR, stringList) : "";
    }

    @Override
    public Set<String> convertToEntityAttribute(String string) {
        return string != null ? Arrays.stream(string.split(SPLIT_CHAR)).collect(Collectors.toSet())
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet())
                : Collections.emptySet();
    }
}

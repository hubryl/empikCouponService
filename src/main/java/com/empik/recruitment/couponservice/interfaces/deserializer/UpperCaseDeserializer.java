package com.empik.recruitment.couponservice.interfaces.deserializer;

import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.Locale;

public class UpperCaseDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) {
        String value = parser.getValueAsString();
        return StringUtils.upperCase(value, Locale.ROOT);
    }
}

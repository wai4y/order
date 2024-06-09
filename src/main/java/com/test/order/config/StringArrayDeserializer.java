package com.test.order.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.test.order.exception.ValidationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StringArrayDeserializer extends JsonDeserializer<String[]> {

    @Override
    public String[] deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        if(!parser.isExpectedStartArrayToken()){
            throw new ValidationException("Invalid String array format");
        }
        List<String> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken() != JsonToken.VALUE_STRING) {
                throw new ValidationException("Invalid String array format");
            }
            list.add(parser.getText());
        }
        return list.toArray(new String[0]);
    }
}


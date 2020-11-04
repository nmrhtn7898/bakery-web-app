package com.bread.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.validation.Errors;

import java.io.IOException;

public class ErrorsSerializer extends JsonSerializer<Errors> {

    @Override
    public Class<Errors> handledType() {
        return Errors.class;
    }

    @Override
    public void serialize(Errors value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeArrayFieldStart("fieldErrors");
        value
                .getFieldErrors()
                .forEach(e -> {
                    try {
                        gen.writeStartObject();
                        gen.writeStringField("field", e.getField());
                        gen.writeStringField("objectName", e.getObjectName());
                        gen.writeStringField("defaultMessage", e.getDefaultMessage());
                        gen.writeStringField("code", e.getCode());
                        Object rejectedValue = e.getRejectedValue();
                        gen.writeStringField(
                                "rejectedValue",
                                rejectedValue != null ? rejectedValue.toString() : ""
                        );
                        gen.writeEndObject();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
        gen.writeEndArray();
        gen.writeArrayFieldStart("globalErrors");
        value
                .getGlobalErrors()
                .forEach(e -> {
                    try {
                        gen.writeStartObject();
                        gen.writeStringField("objectName", e.getObjectName());
                        gen.writeStringField("defaultMessage", e.getDefaultMessage());
                        gen.writeStringField("code", e.getCode());
                        gen.writeEndObject();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
        gen.writeEndArray();
        gen.writeEndObject();
    }
}

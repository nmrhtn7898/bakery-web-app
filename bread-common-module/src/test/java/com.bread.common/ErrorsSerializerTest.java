package com.bread.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;

public class ErrorsSerializerTest {

    public static final ObjectMapper objectMapper;

    static {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new ErrorsSerializer());
        mapper.registerModule(simpleModule);
        objectMapper = mapper;
    }

    @Test
    public void serialize_Success() throws JsonProcessingException {
        Errors errors = new BeanPropertyBindingResult(new TestObject(), "testObject");
        errors.rejectValue("name", "wrongName", "name is wrong");
        errors.rejectValue("age", "wrongAge", "age is wrong");
        errors.reject("wrongObject", "object is wrong");
        String json = objectMapper.writeValueAsString(errors);
        assertThat(json, hasJsonPath("$.fieldErrors[0].field"));
        assertThat(json, hasJsonPath("$.fieldErrors[0].objectName"));
        assertThat(json, hasJsonPath("$.fieldErrors[0].defaultMessage"));
        assertThat(json, hasJsonPath("$.fieldErrors[0].code"));
        assertThat(json, hasJsonPath("$.fieldErrors[1].field"));
        assertThat(json, hasJsonPath("$.fieldErrors[1].objectName"));
        assertThat(json, hasJsonPath("$.fieldErrors[1].defaultMessage"));
        assertThat(json, hasJsonPath("$.fieldErrors[1].code"));
        assertThat(json, hasJsonPath("$.globalErrors[0].objectName"));
        assertThat(json, hasJsonPath("$.globalErrors[0].defaultMessage"));
        assertThat(json, hasJsonPath("$.globalErrors[0].code"));
    }

}

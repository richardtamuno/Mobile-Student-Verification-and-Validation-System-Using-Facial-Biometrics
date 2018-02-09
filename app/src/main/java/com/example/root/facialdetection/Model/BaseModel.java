package com.example.root.facialdetection.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
abstract class BaseModel {
    public Map<String, Object> toDictionary() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(this, Map.class);
    }

    public static <T extends BaseModel> T fromDictionary(Object dictionary, Class<T> t){
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(dictionary, t);
    }
}

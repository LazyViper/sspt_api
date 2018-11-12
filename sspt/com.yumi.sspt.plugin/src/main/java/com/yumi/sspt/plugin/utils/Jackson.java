package com.yumi.sspt.plugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * Jackson 工具类
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年08月12日
 * <p>
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */
public abstract class Jackson {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化为JSON失败", e);
        }
    }

    public static <T> T fromJson(InputStream inputStream, Class<T> klass) {
        String json;
        try {
            json = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("读取输入流失败", e);
        }
        JavaType javaType = objectMapper.getTypeFactory().constructType(klass);
        return fromJsonViaJavaType(json, javaType);
    }

    public static <T> T fromJson(String json, Class<T> klass) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(klass);
        return fromJsonViaJavaType(json, javaType);
    }

    public static <T> T fromJson(String json, Type type) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(type);
        return fromJsonViaJavaType(json, javaType);
    }

    private static <T> T fromJsonViaJavaType(String json, JavaType javaType) {
        try {
            return objectMapper.readerFor(javaType).readValue(json);
        } catch (IOException e) {
            throw new IllegalStateException("反序列化失败", e);
        }
    }

    public static JavaType constructType(Type type) {
        return objectMapper.getTypeFactory().constructType(type);
    }

    public static Map<String, Object> jsonToMap(String json) {
        return jsonToMap(json, String.class, Object.class);
    }

    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            JavaType kType = objectMapper.getTypeFactory().constructType(kClass);
            JavaType vType = objectMapper.getTypeFactory().constructType(vClass);
            MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, kType, vType);
            return objectMapper.readValue(json, mapType);
        } catch (IOException e) {
            throw new IllegalStateException("反序列化失败", e);
        }
    }

    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return fromJsonViaJavaType(json, javaType);
    }

    public static ObjectReader readerFor(JavaType javaType) {
        return objectMapper.readerFor(javaType);
    }

    public static <T> T mapToJavaBean(Map map, Class<T> klass) {
        return objectMapper.convertValue(map, klass);
    }

    public static Map<String, Object> beanToMap(Object o) {
        if (o == null) {
            return null;
        }
        return objectMapper.convertValue(o, Map.class);
    }

    public static JavaType getParametricType(Class<?> paramtricClass, Class<?> elementClass){
        return objectMapper.getTypeFactory().constructParametricType(paramtricClass,elementClass);
    }

    public static <T> T fromJson(String json, Class<T> klass, Class<?> elementClass) {
        JavaType javaType = getParametricType(klass, elementClass);
        return fromJsonViaJavaType(json, javaType);
    }
}

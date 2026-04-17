package com.drp.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * Bean 工具类
 *
 * @author Nick
 */
public class BeanUtil {

    private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);

    /**
     * 私有构造函数
     */
    private BeanUtil() {
    }

    // ==================== 对象属性复制 ====================

    /**
     * 复制对象属性（浅拷贝）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }

    /**
     * 复制对象属性并返回目标对象
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("对象复制失败 | Source: {} | Target: {} | Error: {}",
                    source.getClass(), targetClass, e.getMessage(), e);
            throw new RuntimeException("对象复制失败", e);
        }
    }

    // ==================== 获取空属性名 ====================

    /**
     * 获取对象中为空的属性名
     *
     * @param obj 对象
     * @return 为空的属性名集合
     */
    public static String[] getNullPropertyNames(Object obj) {
        if (obj == null) {
            return new String[0];
        }
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        PropertyDescriptor[] pds = wrapper.getPropertyDescriptors();

        Set<String> nullNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object value = wrapper.getPropertyValue(pd.getName());
            if (value == null) {
                nullNames.add(pd.getName());
            }
        }
        return nullNames.toArray(new String[0]);
    }

    /**
     * 复制对象属性，排除空属性
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target,
                getNullPropertyNames(source));
    }

    /**
     * 复制对象属性，排除空属性并返回目标对象
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @return 目标对象
     */
    public static <T> T copyPropertiesIgnoreNull(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, target,
                    getNullPropertyNames(source));
            return target;
        } catch (Exception e) {
            log.error("对象复制失败 | Source: {} | Target: {} | Error: {}",
                    source.getClass(), targetClass, e.getMessage(), e);
            throw new RuntimeException("对象复制失败", e);
        }
    }

    // ==================== 对象比较 ====================

    /**
     * 比较两个对象是否相等（考虑null）
     */
    public static boolean equals(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    // ==================== JSON 转换 ====================

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            ObjectMapper mapper = getObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("对象转JSON失败 | Error: {}", e.getMessage(), e);
            throw new RuntimeException("对象转JSON失败", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = getObjectMapper();
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON转对象失败 | JSON: {} | Error: {}", json, e.getMessage(), e);
            throw new RuntimeException("JSON转对象失败", e);
        }
    }

    /**
     * 获取 ObjectMapper（线程安全）
     */
    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
}

package com.drp.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文工具类
 *
 * @author Nick
 */
@Component
public class SpringUtil implements ApplicationContextAware {

    /**
     * Spring 应用上下文
     */
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    // ==================== 获取 Bean ====================

    /**
     * 获取 Bean
     *
     * @param clazz Bean 类型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取 Bean（根据名称）
     *
     * @param name Bean 名称
     * @return Bean 实例
     */
    public static Object getBean(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return applicationContext.getBean(name);
    }

    /**
     * 获取 Bean
     *
     * @param name Bean 名称
     * @param clazz Bean 类型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        if (name == null || name.isEmpty() || clazz == null) {
            return null;
        }
        return applicationContext.getBean(name, clazz);
    }

    // ==================== 判断 Bean 是否存在 ====================

    /**
     * 判断 Bean 是否存在
     *
     * @param clazz Bean 类型
     * @return 是否存在
     */
    public static boolean containsBean(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return applicationContext.containsBean(clazz.getSimpleName())
                || applicationContext.containsBean(clazz.getName());
    }

    /**
     * 判断 Bean 是否存在
     *
     * @param name Bean 名称
     * @return 是否存在
     */
    public static boolean containsBean(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return applicationContext.containsBean(name);
    }

    // ==================== 获取 ApplicationContext ====================

    /**
     * 获取 ApplicationContext
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // ==================== 获取环境信息 ====================

    /**
     * 获取环境变量
     *
     * @param key 环境变量名
     * @return 环境变量值
     */
    public static String getEnvProperty(String key) {
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 获取环境变量（带默认值）
     *
     * @param key 环境变量名
     * @param defaultValue 默认值
     * @return 环境变量值
     */
    public static String getEnvProperty(String key, String defaultValue) {
        return applicationContext.getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 获取系统属性
     *
     * @param key 系统属性名
     * @return 系统属性值
     */
    public static String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取系统属性（带默认值）
     *
     * @param key 系统属性名
     * @param defaultValue 默认值
     * @return 系统属性值
     */
    public static String getSystemProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    // ==================== 其他方法 ====================

    /**
     * 动态获取Bean（使用函数式接口）
     *
     * @param clazz Bean 类型
     * @param supplier 获取逻辑
     * @param <T> 类型
     * @return Bean 实例
     */
    public static <T> T getBeanSafely(Class<T> clazz, java.util.function.Supplier<T> supplier) {
        try {
            return getBean(clazz);
        } catch (BeansException e) {
            return supplier.get();
        }
    }
}

package com.drp.common.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author Nick
 */
public class DateUtil {

    /**
     * 标准日期格式
     */
    public static final String PATTERN_DATE = "yyyy-MM-dd";

    /**
     * 标准时间格式
     */
    public static final String PATTERN_TIME = "HH:mm:ss";

    /**
     * 标准日期时间格式
     */
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 紧凑日期格式
     */
    public static final String PATTERN_COMPACT = "yyyyMMdd";

    /**
     * 紧凑时间格式
     */
    public static final String PATTERN_COMPACT_DATETIME = "yyyyMMddHHmmss";

    /**
     * 日期时间格式化器
     */
    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(PATTERN_DATETIME);

    /**
     * 日期格式化器
     */
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(PATTERN_DATE);

    /**
     * 时间格式化器
     */
    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(PATTERN_TIME);

    // ==================== 格式化方法 ====================

    /**
     * 格式化日期时间为标准格式
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间为指定格式
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime == null ? null :
                dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期为标准格式
     */
    public static String format(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期为指定格式
     */
    public static String format(LocalDate date, String pattern) {
        return date == null ? null :
                date.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 解析方法 ====================

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr == null ? null :
                LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 解析日期时间字符串（指定格式）
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        return dateTimeStr == null ? null :
                LocalDateTime.parse(dateTimeStr,
                        DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr == null ? null :
                LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    // ==================== 转换方法 ====================

    /**
     * LocalDateTime 转 Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null :
                Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null :
                date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDate 转 Date
     */
    public static Date toDate(LocalDate date) {
        return date == null ? null :
                Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        return date == null ? null :
                date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ==================== 获取当前时间 ====================

    /**
     * 获取当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    // ==================== 日期计算 ====================

    /**
     * 计算两个日期之间的天数
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return Duration.between(start, end).toDays();
    }

    /**
     * 计算两个日期时间之间的小时数
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toHours();
    }

    /**
     * 计算两个日期时间之间的分钟数
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * 判断是否是今天
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(today());
    }

    /**
     * 判断是否是过去
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(now());
    }

    /**
     * 判断是否是未来
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(now());
    }
}

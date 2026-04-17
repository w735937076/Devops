package com.drp.common.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花ID生成器
 * <p>
 * Twitter Snowflake 算法实现，用于生成分布式唯一ID
 *
 * @author Nick
 */
public class SnowflakeIdUtil {

    // ==================== 时间戳相关 ====================

    /**
     * 开始时间戳（2024-01-01 00:00:00）
     */
    private static final long EPOCH = 1704067200000L;

    /**
     * 时间戳位数
     */
    private static final long TIMESTAMP_BITS = 41L;

    // ==================== 机器ID相关 ====================

    /**
     * 机器ID位数
     */
    private static final long MACHINE_BITS = 10L;

    /**
     * 最大机器ID (2^10 - 1 = 1023)
     */
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_BITS);

    /**
     * 机器ID向左偏移位数
     */
    private static final long MACHINE_SHIFT = TIMESTAMP_BITS;

    // ==================== 序列号相关 ====================

    /**
     * 序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 最大序列号 (2^12 - 1 = 4095)
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 序列号掩码
     */
    private static final long SEQUENCE_MASK = MAX_SEQUENCE;

    // ==================== 实例变量 ====================

    /**
     * 机器ID
     */
    private final long machineId;

    /**
     * 序列号
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param machineId 机器ID（0~1023）
     */
    public SnowflakeIdUtil(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException(
                    String.format("Machine ID must be between 0 and %d", MAX_MACHINE_ID));
        }
        this.machineId = machineId;
    }

    // ==================== ID生成方法 ====================

    /**
     * 生成下一个ID
     *
     * @return 分布式唯一ID
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                // 等待时间差恢复
                try {
                    wait(offset << 1);
                    timestamp = currentTimeMillis();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException("Clock moved backwards");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting", e);
                }
            } else {
                throw new RuntimeException(
                        String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
            }
        }

        // 同一毫秒内，序列号递增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 序列号溢出，等待下一毫秒
                timestamp = waitUntilNextMillis(timestamp);
            }
        } else {
            // 新毫秒，序列号重置
            sequence = ThreadLocalRandom.current().nextLong(0, 1024);
        }

        lastTimestamp = timestamp;

        // 组装ID
        return ((timestamp - EPOCH) << (MACHINE_SHIFT + SEQUENCE_BITS - TIMESTAMP_BITS))
                | (machineId << SEQUENCE_BITS)
                | sequence;
    }

    /**
     * 生成下一个ID字符串
     *
     * @return ID字符串
     */
    public String nextIdStr() {
        return String.valueOf(nextId());
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前时间戳（毫秒）
     */
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫秒
     */
    private long waitUntilNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    // ==================== 静态方法 ====================

    /**
     * 默认实例（机器ID为0）
     */
    private static final SnowflakeIdUtil DEFAULT_INSTANCE = new SnowflakeIdUtil(0);

    /**
     * 生成全局唯一ID
     *
     * @return 分布式唯一ID
     */
    public static long generateId() {
        return DEFAULT_INSTANCE.nextId();
    }

    /**
     * 生成全局唯一ID字符串
     *
     * @return ID字符串
     */
    public static String generateIdStr() {
        return DEFAULT_INSTANCE.nextIdStr();
    }

    /**
     * 解析ID获取时间戳
     *
     * @param id 分布式ID
     * @return 时间戳
     */
    public static long getTimestamp(long id) {
        return (id >> (MACHINE_SHIFT + SEQUENCE_BITS - TIMESTAMP_BITS)) + EPOCH;
    }

    /**
     * 解析ID获取机器ID
     *
     * @param id 分布式ID
     * @return 机器ID
     */
    public static long getMachineId(long id) {
        return (id >> SEQUENCE_BITS) & MAX_MACHINE_ID;
    }

    /**
     * 解析ID获取序列号
     *
     * @param id 分布式ID
     * @return 序列号
     */
    public static long getSequence(long id) {
        return id & SEQUENCE_MASK;
    }
}

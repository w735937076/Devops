package com.drp.common.result;

/**
 * 追踪上下文
 * <p>
 * 用于在请求链路中传递追踪ID
 *
 * @author Nick
 */
public class TraceContext {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    /**
     * 获取当前追踪ID
     */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /**
     * 设置追踪ID
     */
    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    /**
     * 清除追踪ID
     */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }

    /**
     * 生成新的追踪ID
     */
    public static String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}

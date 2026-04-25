package com.drp.log.aspect;

import com.drp.log.annotation.OperationLog;
import com.drp.log.dto.OperationLogCreateRequest;
import com.drp.log.enums.OperationType;
import com.drp.log.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        HttpServletRequest request = getHttpServletRequest();
        Long operatorId = getCurrentUserId();
        String operator = getCurrentUsername();

        long start = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;

            // Lambda 中使用的变量必须是 final 或 effectively final
            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;
            final long finalDuration = duration;

            CompletableFuture.runAsync(() -> {
                try {
                    OperationLogCreateRequest createRequest = new OperationLogCreateRequest();
                    createRequest.setOperator(operator);
                    createRequest.setOperatorId(operatorId);
                    createRequest.setOperationType(operationLog.type().name());
                    createRequest.setProjectId(getParamValue(point, operationLog.projectParam()));
                    createRequest.setEnv(getParamValueAsString(point, operationLog.envParam()));
                    createRequest.setVersion(getParamValueAsString(point, operationLog.versionParam()));
                    createRequest.setDetail(getMethodParams(point));
                    createRequest.setIp(getClientIp(request));
                    createRequest.setStatus(finalSuccess ? "SUCCESS" : "FAIL");
                    createRequest.setDuration(finalDuration);
                    createRequest.setErrorMessage(finalErrorMessage);

                    operationLogService.create(createRequest);
                } catch (Exception e) {
                    log.error("记录操作日志失败", e);
                }
            });
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Object userId = attributes.getRequest().getAttribute("userId");
                if (userId instanceof Long) {
                    return (Long) userId;
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
        }
        return null;
    }

    private String getCurrentUsername() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Object username = attributes.getRequest().getAttribute("username");
                if (username instanceof String) {
                    return (String) username;
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户名失败", e);
        }
        return "SYSTEM";
    }

    private Long getParamValue(ProceedingJoinPoint point, String paramName) {
        if (paramName == null || paramName.isEmpty()) {
            return null;
        }
        try {
            Object[] args = point.getArgs();
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = signature.getParameterNames();

            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName)) {
                    Object arg = args[i];
                    if (arg instanceof Long) {
                        return (Long) arg;
                    } else if (arg instanceof Integer) {
                        return ((Integer) arg).longValue();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取参数值失败: {}", paramName, e);
        }
        return null;
    }

    private String getParamValueAsString(ProceedingJoinPoint point, String paramName) {
        if (paramName == null || paramName.isEmpty()) {
            return null;
        }
        try {
            Object[] args = point.getArgs();
            MethodSignature signature = (MethodSignature) point.getSignature();
            String[] paramNames = signature.getParameterNames();

            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName)) {
                    Object arg = args[i];
                    return arg != null ? arg.toString() : null;
                }
            }
        } catch (Exception e) {
            log.warn("获取参数值失败: {}", paramName, e);
        }
        return null;
    }

    private String getMethodParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            MethodSignature signature = (MethodSignature) point.getSignature();
            String[] paramNames = signature.getParameterNames();

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (int i = 0; i < paramNames.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(paramNames[i]).append("\":");
                Object arg = args[i];
                if (arg == null) {
                    sb.append("null");
                } else if (arg instanceof String) {
                    sb.append("\"").append(arg).append("\"");
                } else if (arg instanceof Number) {
                    sb.append(arg);
                } else {
                    sb.append("\"").append(arg.toString()).append("\"");
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            log.warn("获取方法参数失败", e);
            return "{}";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

package com.drp.common.config;

import com.drp.common.result.TraceContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;

/**
 * Web 配置类
 *
 * @author Nick
 */
@Configuration
public class WebConfig {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    /**
     * 追踪ID过滤器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter tracingIdFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                // 获取或生成追踪ID
                String traceId = httpRequest.getHeader("X-Trace-Id");
                if (traceId == null || traceId.isEmpty()) {
                    traceId = TraceContext.generateTraceId();
                }
                TraceContext.setTraceId(traceId);
                httpResponse.setHeader("X-Trace-Id", traceId);

                try {
                    chain.doFilter(request, response);
                } finally {
                    TraceContext.clear();
                }
            }
        };
    }

    /**
     * CORS 配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（生产环境应限制）
        config.addAllowedOriginPattern("*");
        // 允许凭证
        config.setAllowCredentials(true);
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有方法
        config.addAllowedMethod("*");
        // 暴露响应头
        config.addExposedHeader("X-Trace-Id");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

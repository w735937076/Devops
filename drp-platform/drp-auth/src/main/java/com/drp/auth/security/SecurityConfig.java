package com.drp.auth.security;

import com.drp.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置类
 * <p>
 * 配置认证流程、授权规则、密码加密等
 *
 * @author Nick
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 认证失败时的 Entry Point
     */
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * JWT 认证过滤器
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 用户详情服务
     */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * 密码编码器
     */
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(JwtAuthenticationEntryPoint authenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         UserDetailsServiceImpl userDetailsService) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤器链
     *
     * @param http HttpSecurity 对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("配置 Security Filter Chain...");

        http
                // 禁用 CSRF (使用 JWT 无状态认证)
                .csrf(AbstractHttpConfigurer::disable)

                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置会话管理 (使用无状态会话)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        // 认证失败处理
                        .authenticationEntryPoint(authenticationEntryPoint)
                        // 授权失败处理
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("授权失败: {}", accessDeniedException.getMessage());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            Result<?> result = Result.error(403, "权限不足");
                            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
                        })
                )

                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/captcha").permitAll()
                        // WebSocket 端点
                        .requestMatchers("/ws/**").permitAll()
                        // Actuator 端点 (生产环境应限制)
                        .requestMatchers("/actuator/**").permitAll()
                        // 静态资源
                        .requestMatchers(HttpMethod.GET, "/static/**", "/favicon.ico").permitAll()
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )

                // 添加 JWT 认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 禁用匿名用户
                .anonymous(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * 配置认证提供者
     *
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // 不隐藏用户不存在，以防止用户名枚举攻击
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * 配置认证管理器
     *
     * @param config 认证配置
     * @return AuthenticationManager
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置密码编码器
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 CORS
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源 (生产环境应限制)
        configuration.setAllowedOriginPatterns(List.of("*"));
        // 允许所有请求头
        configuration.setAllowedHeaders(List.of("*"));
        // 允许所有方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许凭证
        configuration.setAllowCredentials(true);
        // 暴露响应头
        configuration.setExposedHeaders(List.of("Authorization", "X-Trace-Id"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

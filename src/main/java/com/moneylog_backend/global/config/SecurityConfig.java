package com.moneylog_backend.global.config;

import com.moneylog_backend.global.auth.jwt.JwtAuthenticationFilter;
import com.moneylog_backend.global.auth.jwt.JwtProperties;
import com.moneylog_backend.global.auth.jwt.JwtProvider;
import com.moneylog_backend.global.auth.jwt.RedisTokenKeyResolver;
import com.moneylog_backend.global.auth.security.RestAccessDeniedHandler;
import com.moneylog_backend.global.auth.security.RestAuthenticationEntryPoint;
import com.moneylog_backend.global.auth.security.CustomUserDetailsService;
import com.moneylog_backend.global.util.RedisService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final JwtProperties jwtProperties;
    private final RedisTokenKeyResolver redisTokenKeyResolver;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Value("${app.security.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // 1. CSRF 비활성화
            .csrf(AbstractHttpConfigurer::disable)
            // 2. Form Login & Http Basic 비활성화
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // 3. CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler))
            // 4. 세션 관리 정책 설정 : Stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 5. 요청별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .requestMatchers("/",
                                 "/api/user/signup",
                                 "/api/user/login",
                                 "/api/user/refresh",
                                 "/api/bank",
                                 "/api/files/view")
                .permitAll()
                .requestMatchers("/api/admin/**")
                .hasAuthority("ADMIN")
                .anyRequest()
                .authenticated())
            .addFilterBefore(jwtAuthenticationFilter(restAuthenticationEntryPoint),
                             UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter (AuthenticationEntryPoint authenticationEntryPoint) {
        return new JwtAuthenticationFilter(customUserDetailsService, jwtProvider, redisService, jwtProperties,
                                           redisTokenKeyResolver, authenticationEntryPoint);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource () {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                                                 .map(String::trim)
                                                 .filter(origin -> !origin.isBlank())
                                                 .collect(Collectors.toList()));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}

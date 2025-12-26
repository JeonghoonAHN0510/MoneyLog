package com.moneylog_backend.global.auth.jwt;

import com.moneylog_backend.global.util.RedisService;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Request Header에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검사
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            // ⭐ Redis에 블랙리스트("BL:토큰값")로 등록되어 있는지 확인
            String isLogout = redisService.getValues("BL:" + token);

            if (isLogout != null) {
                // 블랙리스트면 에러 처리 또는 필터 통과 X
                // 여기선 단순 로그만 찍고 넘어가거나, 예외를 던질 수 있음
                throw new RuntimeException("로그아웃된 토큰입니다.");
            } // if end
            // 3. 토큰이 유효하면 인증 객체(Authentication)를 만들어서 SecurityContext에 저장
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } // if end

        filterChain.doFilter(request, response);
    } // func end

    // 헤더에서 "Bearer " 떼고 순수 토큰만 가져오기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        } // if end
        return null;
    } // func end
} // class end
package com.moneylog_backend.global.auth.jwt;

import com.moneylog_backend.global.auth.security.CustomUserDetailsService;
import com.moneylog_backend.global.util.RedisService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        // 1. Request Header에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검사
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            // 3. ⭐ Redis 블랙리스트 확인 (로그아웃된 토큰인지)
            // 키: "BL:토큰값" 이 존재하면 로그아웃된 상태임
            if (redisService.hasKey("BL:" + token)) {
                // 로그아웃된 토큰으로 접근 시 에러 처리 (여기선 간단히 401 에러로 넘기거나 예외 발생)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 사용자입니다.");
                return; // 필터 체인 중단
            }
            try {
                Authentication authentication = jwtProvider.getAuthentication(token);
                String loginId = authentication.getName();
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                request.setAttribute("error", e.getMessage());
            }

        }

        filterChain.doFilter(request, response);
    }

    // 헤더에서 "Bearer " 떼고 순수 토큰만 가져오기
    private String resolveToken (HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
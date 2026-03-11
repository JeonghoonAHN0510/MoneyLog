package com.moneylog_backend.global.auth.jwt;

import com.moneylog_backend.global.auth.security.CustomUserDetailsService;
import com.moneylog_backend.global.util.RedisService;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
        "/api/user/signup",
        "/api/user/login",
        "/api/user/refresh",
        "/api/user/password-reset/request",
        "/api/user/password-reset/verify-otp",
        "/api/user/password-reset/confirm"
    );

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final JwtProperties jwtProperties;
    private final RedisTokenKeyResolver redisTokenKeyResolver;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_AUTH_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && !jwtProvider.validateToken(token)) {
            commenceUnauthorized(request, response, new BadCredentialsException("유효하지 않은 인증 토큰입니다."));
            return;
        }

        if (StringUtils.hasText(token)) {
            if (redisService.hasKey(redisTokenKeyResolver.blacklist(token))) {
                commenceUnauthorized(request, response, new BadCredentialsException("로그아웃된 사용자입니다."));
                return;
            }

            try {
                Authentication authentication = jwtProvider.getAuthentication(token);
                String loginId = authentication.getName();
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);

                long remainingTime = jwtProvider.getExpiration(token);
                long refreshThresholdMillis = jwtProperties.getAccessTokenRefreshThresholdInSeconds() * 1000;

                if (remainingTime > 0 && remainingTime < refreshThresholdMillis) {
                    String newToken = jwtProvider.createAccessToken(authentication);
                    response.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + newToken);
                }

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                log.error("JWT 인증 필터 처리 중 예상치 못한 오류 발생", e);
                commenceUnauthorized(
                    request,
                    response,
                    new AuthenticationServiceException("인증 처리 중 오류가 발생했습니다.", e)
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void commenceUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException authenticationException)
        throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, authenticationException);
    }
}

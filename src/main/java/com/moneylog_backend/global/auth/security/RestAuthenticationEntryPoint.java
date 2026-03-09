package com.moneylog_backend.global.auth.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence (HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {
        writeErrorResponse(response, request, HttpStatus.UNAUTHORIZED,
                           resolveMessage(authException));
    }

    private String resolveMessage (AuthenticationException authException) {
        String message = authException.getMessage();
        if (authException instanceof InsufficientAuthenticationException || message == null || message.isBlank()
            || message.startsWith("Full authentication is required")) {
            return "인증이 필요합니다.";
        }
        return message;
    }

    private void writeErrorResponse (HttpServletResponse response, HttpServletRequest request, HttpStatus status,
                                     String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), securityErrorBody(status, message, request.getRequestURI()));
    }

    private Map<String, Object> securityErrorBody (HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}

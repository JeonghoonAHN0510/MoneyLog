package com.moneylog_backend.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {
    public String getLoginId (Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }
}

package com.moneylog_backend.global.util;

import org.springframework.security.access.AccessDeniedException;

public final class OwnershipValidator {
    private OwnershipValidator() {
    }

    public static void validateOwner (Integer resourceUserId, Integer userId, String deniedMessage) {
        if (resourceUserId == null || userId == null || !resourceUserId.equals(userId)) {
            throw new AccessDeniedException(deniedMessage);
        }
    }
}

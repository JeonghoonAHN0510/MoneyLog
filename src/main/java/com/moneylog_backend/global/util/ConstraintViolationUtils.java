package com.moneylog_backend.global.util;

import org.springframework.dao.DataIntegrityViolationException;

public final class ConstraintViolationUtils {
    private ConstraintViolationUtils() {
    }

    public static boolean causedByConstraint(DataIntegrityViolationException exception, String constraintName) {
        if (exception == null || constraintName == null || constraintName.isBlank()) {
            return false;
        }

        String normalizedConstraintName = constraintName.toLowerCase();
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains(normalizedConstraintName)) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }
}

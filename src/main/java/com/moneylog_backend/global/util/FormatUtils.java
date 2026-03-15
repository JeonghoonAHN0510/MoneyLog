package com.moneylog_backend.global.util;

import org.springframework.stereotype.Component;

@Component
public class FormatUtils {

    public String toPhone (String userPhone) {
        String digits = InputStringNormalizer.digitsOnly(userPhone);
        if (digits.isEmpty()) {
            return "";
        }

        if (digits.length() == 10) {
            return digits.replaceAll("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }

        if (digits.length() == 11) {
            return digits.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }

        return digits;
    }

    public String toCurrency (int amount) {
        return String.format("%,d", amount);
    }
}

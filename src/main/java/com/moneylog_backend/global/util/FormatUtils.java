package com.moneylog_backend.global.util;

import org.springframework.stereotype.Component;

@Component
public class FormatUtils {

    public String toPhone(String userPhone){
        if (userPhone == null) return "";
        return userPhone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }

    public String toCurrency(int amount) {
        return String.format("%,d", amount);
    }
}
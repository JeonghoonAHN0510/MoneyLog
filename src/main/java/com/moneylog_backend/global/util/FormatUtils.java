package com.moneylog_backend.global.util;

public class FormatUtils {
    private FormatUtils() {}

    public static String toPhone(String userPhone){
        if (userPhone == null) return "";
        return userPhone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }

    public static String toCurrency(int amount) {
        return String.format("%,d", amount);
    }
}
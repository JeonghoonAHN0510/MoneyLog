package com.moneylog_backend.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankAccountNumberFormatter {

    @AllArgsConstructor
    @Getter
    private static class PatternRule {
        private String regex;
        private String replacement;
    }

    // 하나의 은행이 여러 개의 계좌 패턴을 가질 수 있으므로 List로 관리
    private static final Map<String, List<PatternRule>> BANK_PATTERNS = new HashMap<>();

    static {
        addPattern("KB국민은행", "^(\\d{6})(\\d{2})(\\d{6})$", "$1-$2-$3");             // 신계좌
        addPattern("KB국민은행", "^(\\d{4})(\\d{2})(\\d{6})$", "$1-$2-$3");             // 구계좌
        addPattern("신한은행", "^(\\d{3})(\\d{3})(\\d{6})$", "$1-$2-$3");
        addPattern("우리은행", "^(\\d{4})(\\d{3})(\\d{6})$", "$1-$2-$3");
        addPattern("하나은행", "^(\\d{3})(\\d{6})(\\d{5})$", "$1-$2-$3");
        addPattern("NH농협은행", "^(\\d{3})(\\d{4})(\\d{4})(\\d{2})$", "$1-$2-$3-$4");  // 중앙회/단위농협
        addPattern("NH농협은행", "^(\\d{3})(\\d{4})(\\d{4})$", "$1-$2-$3");             // 구계좌
        addPattern("IBK기업은행", "^(\\d{3})(\\d{6})(\\d{2})(\\d{3})$", "$1-$2-$3-$4");
        addPattern("SC제일은행", "^(\\d{3})(\\d{2})(\\d{6})$", "$1-$2-$3");
        addPattern("한국씨티은행", "^(\\d{3})(\\d{6})(\\d{3})$", "$1-$2-$3");
        addPattern("카카오뱅크", "^(\\d{4})(\\d{2})(\\d{7})$", "$1-$2-$3");
        addPattern("토스뱅크", "^(\\d{4})(\\d{4})(\\d{4})$", "$1-$2-$3");
        addPattern("케이뱅크", "^(\\d{3})(\\d{3})(\\d{6})$", "$1-$2-$3");
        addPattern("DGB대구은행", "^(\\d{3})(\\d{2})(\\d{7})$", "$1-$2-$3");
        addPattern("DGB대구은행", "^(\\d{3})(\\d{2})(\\d{6})(\\d{1})$", "$1-$2-$3-$4");
        addPattern("BNK부산은행", "^(\\d{3})(\\d{4})(\\d{4})(\\d{2})$", "$1-$2-$3-$4");
        addPattern("BNK경남은행", "^(\\d{3})(\\d{2})(\\d{7})$", "$1-$2-$3");
        addPattern("광주은행", "^(\\d{3})(\\d{3})(\\d{7})$", "$1-$2-$3");
        addPattern("전북은행", "^(\\d{3})(\\d{3})(\\d{7})$", "$1-$2-$3");
        addPattern("제주은행", "^(\\d{3})(\\d{2})(\\d{7})$", "$1-$2-$3");
        addPattern("제주은행", "^(\\d{3})(\\d{2})(\\d{5})$", "$1-$2-$3");
        addPattern("KDB산업은행", "^(\\d{3})(\\d{4})(\\d{4})(\\d{3})$", "$1-$2-$3-$4");
        addPattern("수협은행", "^(\\d{3})(\\d{2})(\\d{7})$", "$1-$2-$3");
        addPattern("수협은행", "^(\\d{3})(\\d{2})(\\d{6})$", "$1-$2-$3");
        addPattern("우체국", "^(\\d{6})(\\d{2})(\\d{6})$", "$1-$2-$3");
        addPattern("새마을금고", "^(\\d{4})(\\d{3})(\\d{6})$", "$1-$2-$3");
        addPattern("신협", "^(\\d{3})(\\d{2})(\\d{6})(\\d{2})$", "$1-$2-$3-$4");
        addPattern("신협", "^(\\d{3})(\\d{4})(\\d{4})(\\d{2})$", "$1-$2-$3-$4");
        addPattern("저축은행", "^(\\d{3})(\\d{2})(\\d{2})(\\d{7})$", "$1-$2-$3-$4");
        addPattern("한국은행", "^(\\d{3})(\\d{3})(\\d{6})$", "$1-$2-$3");
    }

    // 패턴 등록 헬퍼 메서드
    private static void addPattern (String bankName, String regex, String replacement) {
        BANK_PATTERNS.computeIfAbsent(bankName, k -> new ArrayList<>()).add(new PatternRule(regex, replacement));
    }

    public static String format (String bankName, String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return "";
        }

        String cleanNumber = accountNumber.replaceAll("[^0-9]", "");

        List<PatternRule> rules = BANK_PATTERNS.get(bankName);

        if (rules == null || rules.isEmpty()) {
            return cleanNumber;
        }

        for (PatternRule rule : rules) {
            if (cleanNumber.matches(rule.getRegex())) {
                return cleanNumber.replaceAll(rule.getRegex(), rule.getReplacement());
            }
        }

        return cleanNumber;
    }
}
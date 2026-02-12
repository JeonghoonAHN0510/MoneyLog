package com.moneylog_backend.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleEnum {
    DAILY("DAILY") {
        @Override
        public String toCron(int minute, int hour, Integer dayOfWeek, Integer dayOfMonth) {
            return String.format("0 %d %d * * ?", minute, hour);
        }
    },
    WEEKLY("WEEKLY") {
        @Override
        public String toCron(int minute, int hour, Integer dayOfWeek, Integer dayOfMonth) {
            if (dayOfWeek == null) throw new IllegalArgumentException("WEEKLY requires dayOfWeek (1-7)");
            // Quartz mapping: 1(MON)->2, ..., 7(SUN)->1
            int quartzDay = (dayOfWeek % 7) + 1;
            return String.format("0 %d %d ? * %d", minute, hour, quartzDay);
        }
    },
    MONTHLY("MONTHLY") {
        @Override
        public String toCron(int minute, int hour, Integer dayOfWeek, Integer dayOfMonth) {
            if (dayOfMonth == null) throw new IllegalArgumentException("MONTHLY requires dayOfMonth (1-31)");
            return String.format("0 %d %d %d * ?", minute, hour, dayOfMonth);
        }
    };

    private final String frequency;

    // 각 Enum 상수에서 구현할 추상 메소드
    public abstract String toCron(int minute, int hour, Integer dayOfWeek, Integer dayOfMonth);

    // 문자열을 Enum으로 안전하게 변환하는 helper 메소드
    public static ScheduleEnum fromString(String text) {
        for (ScheduleEnum s : ScheduleEnum.values()) {
            if (s.frequency.equalsIgnoreCase(text)) return s;
        }
        throw new IllegalArgumentException("Unknown frequency: " + text);
    }
}
package com.moneylog_backend.global.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장 (Key, Value, 만료시간)
    public void setValues(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration.toMillis(), TimeUnit.MILLISECONDS);
    } // func end

    // 데이터 조회
    public String getValues(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : value.toString();
    } // func end

    // 데이터 삭제
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    } // func end

    // 키 존재 여부 확인 (블랙리스트 체크용)
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    } // func end
} // class end
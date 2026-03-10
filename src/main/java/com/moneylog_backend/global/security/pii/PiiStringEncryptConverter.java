package com.moneylog_backend.global.security.pii;

import com.moneylog_backend.global.config.SpringContextHolder;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PiiStringEncryptConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return SpringContextHolder.getBean(PiiCryptoService.class).encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return SpringContextHolder.getBean(PiiCryptoService.class).decrypt(dbData);
    }
}

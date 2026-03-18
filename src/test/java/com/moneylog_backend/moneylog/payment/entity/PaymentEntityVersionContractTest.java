package com.moneylog_backend.moneylog.payment.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Version;

class PaymentEntityVersionContractTest {
    @Test
    void payment_entity는_version_필드를_가진다() throws Exception {
        Field versionField = PaymentEntity.class.getDeclaredField("version");

        assertNotNull(versionField.getAnnotation(Version.class));
    }
}

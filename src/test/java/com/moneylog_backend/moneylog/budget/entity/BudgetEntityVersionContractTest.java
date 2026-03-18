package com.moneylog_backend.moneylog.budget.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Version;

class BudgetEntityVersionContractTest {
    @Test
    void budget_entity는_version_필드를_가진다() throws Exception {
        Field versionField = BudgetEntity.class.getDeclaredField("version");

        assertNotNull(versionField.getAnnotation(Version.class));
    }
}

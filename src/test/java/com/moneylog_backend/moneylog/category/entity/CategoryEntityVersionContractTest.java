package com.moneylog_backend.moneylog.category.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Version;

class CategoryEntityVersionContractTest {
    @Test
    void category_entity는_version_필드를_가진다() throws Exception {
        Field versionField = CategoryEntity.class.getDeclaredField("version");

        assertNotNull(versionField.getAnnotation(Version.class));
    }
}

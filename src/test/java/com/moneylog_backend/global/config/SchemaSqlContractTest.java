package com.moneylog_backend.global.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SchemaSqlContractTest {
    private static final Path SCHEMA_SQL = Path.of("src/main/resources/sql/schema.sql");

    @Test
    void schema_sql은_동시성_후속_version_backfill_DDL을_포함한다() throws IOException {
        String schemaSql = Files.readString(SCHEMA_SQL, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(schemaSql.contains("alter table category add column if not exists version bigint not null default 0;")),
            () -> assertTrue(schemaSql.contains("alter table payment add column if not exists version bigint not null default 0;")),
            () -> assertTrue(schemaSql.contains("alter table budget add column if not exists version bigint not null default 0;")),
            () -> assertTrue(schemaSql.contains("alter table job_metadata add column if not exists version bigint not null default 0;"))
        );
    }
}

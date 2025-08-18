package com.kt.kol.api.debug.controller;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/debug")
@Slf4j
public class DebugController {

    private final DatabaseClient databaseClient;

    @GetMapping("/tables")
    public Mono<String> showTables() {
        return databaseClient.sql("SELECT TABLE_NAME, TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'")
                .fetch()
                .all()
                .collectList()
                .map(tables -> "Tables: " + tables.toString())
                .onErrorReturn("Error fetching tables");
    }

    @GetMapping("/policy-data")
    public Mono<String> showPolicyData() {
        return databaseClient.sql("SELECT * FROM KOL_ENCRYPTION_POLICY")
                .fetch()
                .all()
                .collectList()
                .map(data -> "Policy Data: " + data.toString())
                .onErrorReturn("Error fetching policy data");
    }

    @GetMapping("/key-data")
    public Mono<String> showKeyData() {
        return databaseClient.sql("SELECT KEY_ID, CLIENT_ID, ALGORITHM, KEY_VERSION, STATUS FROM KOL_ENCRYPTION_KEY")
                .fetch()
                .all()
                .collectList()
                .map(data -> "Key Data: " + data.toString())
                .onErrorReturn("Error fetching key data");
    }

    @GetMapping("/flyway-history")
    public Mono<String> showFlywayHistory() {
        return databaseClient.sql("SELECT * FROM FLYWAY_SCHEMA_HISTORY")
                .fetch()
                .all()
                .collectList()
                .map(data -> "Flyway History: " + data.toString())
                .onErrorReturn("No Flyway history found");
    }

    @GetMapping("/setup-db")
    public Mono<String> setupDatabase() {
        return databaseClient.sql("DROP TABLE IF EXISTS KOL_ENCRYPTION_POLICY")
                .then()
                .then(databaseClient.sql("DROP TABLE IF EXISTS KOL_ENCRYPTION_KEY").then())
                .then(databaseClient.sql("""
                    CREATE TABLE KOL_ENCRYPTION_POLICY (
                        POLICY_ID        VARCHAR(32) PRIMARY KEY,
                        CLIENT_ID        VARCHAR(64) NOT NULL,
                        API_PATH         VARCHAR(256) NOT NULL,
                        HTTP_METHOD      VARCHAR(8) NOT NULL,
                        FIELD_NAME       VARCHAR(128) NOT NULL,
                        DIRECTION        VARCHAR(8) NOT NULL,
                        STATUS           CHAR(1) DEFAULT 'A'
                    )
                    """).then())
                .then(databaseClient.sql("""
                    CREATE TABLE KOL_ENCRYPTION_KEY (
                        KEY_ID           VARCHAR(32) PRIMARY KEY,
                        CLIENT_ID        VARCHAR(64) NOT NULL,
                        ALGORITHM        VARCHAR(32) NOT NULL,
                        KEY_MATERIAL     VARBINARY(512) NOT NULL,
                        KEY_VERSION      INT DEFAULT 1,
                        STATUS           CHAR(1) DEFAULT 'A'
                    )
                    """).then())
                .then(databaseClient.sql("""
                    INSERT INTO KOL_ENCRYPTION_POLICY (POLICY_ID, CLIENT_ID, API_PATH, HTTP_METHOD, FIELD_NAME, DIRECTION, STATUS) VALUES
                    ('POLICY_001', 'MOBILE_APP', '/api/users/profile', 'POST', 'personalId', 'IN', 'A'),
                    ('POLICY_002', 'MOBILE_APP', '/api/users/profile', 'POST', 'phoneNumber', 'BOTH', 'A')
                    """).then())
                .then(databaseClient.sql("""
                    INSERT INTO KOL_ENCRYPTION_KEY (KEY_ID, CLIENT_ID, ALGORITHM, KEY_MATERIAL, KEY_VERSION, STATUS) VALUES
                    ('KEY_MOBILE_001', 'MOBILE_APP', 'AES256', X'1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF', 1, 'A')
                    """).then())
                .then(Mono.just("✅ Database setup completed! Tables created and sample data inserted."))
                .onErrorReturn("❌ Error setting up database");
    }
}
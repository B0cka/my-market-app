package com.b0cka.cont;


import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public final class PostgreContainer1 {

    @Container
    @ServiceConnection
    public static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("junit")
                    .withPassword("junit");



}
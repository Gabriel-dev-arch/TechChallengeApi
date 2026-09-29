package com.techchallenge.oficina.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Sobe um PostgreSQL descartável (Testcontainers) e o conecta automaticamente ao DataSource.
 * Uso: {@code @Import(PostgresTestConfiguration.class)} em qualquer teste de integração.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer("postgres:17-alpine");
	}
}

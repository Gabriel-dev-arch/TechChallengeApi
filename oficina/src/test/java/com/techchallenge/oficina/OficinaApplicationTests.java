package com.techchallenge.oficina;

import com.techchallenge.oficina.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class OficinaApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void flywayAplicaMigrationBaselineNoPostgres() {
		Integer aplicadas = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where success = true", Integer.class);

		assertThat(aplicadas).isGreaterThanOrEqualTo(1);
	}

}

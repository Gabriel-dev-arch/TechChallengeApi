package com.techchallenge.oficina.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConfigTest {

	private final ApplicationContextRunner contexto = new ApplicationContextRunner()
			.withUserConfiguration(JwtConfig.class);

	@Test
	void naoSobeComSegredoDeMenosDe32Caracteres() {
		contexto.withPropertyValues("app.seguranca.jwt.segredo=" + "a".repeat(31)).run(ctx -> {
			assertThat(ctx).hasFailed();
			assertThat(ctx.getStartupFailure())
					.hasStackTraceContaining("JWT_SECRET precisa ter pelo menos 32 caracteres. Veja o .env.example");
		});
	}

	@Test
	void naoSobeSemSegredo() {
		contexto.withPropertyValues("app.seguranca.jwt.segredo=").run(ctx -> {
			assertThat(ctx).hasFailed();
			assertThat(ctx.getStartupFailure()).hasStackTraceContaining("JWT_SECRET não foi definido");
		});
	}

	@Test
	void sobeComSegredoDe32CaracteresEExpiracaoPadraoDe60Minutos() {
		contexto.withPropertyValues("app.seguranca.jwt.segredo=" + "a".repeat(32)).run(ctx -> {
			assertThat(ctx).hasNotFailed().hasSingleBean(JwtEncoder.class).hasSingleBean(JwtDecoder.class);
			assertThat(ctx.getBean(JwtProperties.class).expiracaoMinutos()).isEqualTo(60);
		});
	}
}

package com.techchallenge.oficina.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuração do JWT ({@code app.seguranca.jwt}), validada na inicialização: com segredo ausente ou curto
 * a aplicação não sobe, em vez de falhar só no primeiro login.
 */
@Validated
@ConfigurationProperties(prefix = "app.seguranca.jwt")
public record JwtProperties(String segredo, @DefaultValue("60") @Positive long expiracaoMinutos) {

	/** O HS256 exige chave de pelo menos 256 bits (32 bytes). */
	private static final int TAMANHO_MINIMO = 32;

	// O segredo é validado nestes métodos, e não com @NotBlank/@Size no campo, porque o relatório de falha
	// do Spring Boot imprime o valor rejeitado do campo, o que exporia o segredo no log.

	@AssertTrue(message = "JWT_SECRET não foi definido. Copie o .env.example para .env ou defina a variável de ambiente")
	public boolean isSegredoDefinido() {
		return StringUtils.hasText(segredo);
	}

	@AssertTrue(message = "JWT_SECRET precisa ter pelo menos 32 caracteres. Veja o .env.example")
	public boolean isSegredoComTamanhoMinimo() {
		return !StringUtils.hasText(segredo) || segredo.length() >= TAMANHO_MINIMO;
	}

	public SecretKey chave() {
		return new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}

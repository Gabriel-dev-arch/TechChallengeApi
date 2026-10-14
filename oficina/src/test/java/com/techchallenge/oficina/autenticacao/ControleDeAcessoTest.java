package com.techchallenge.oficina.autenticacao;

import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ControleDeAcessoTest {

	private static final String CPF_DO_CLIENTE = "52998224725";
	private static final String CPF_DE_OUTRO_CLIENTE = "11144477735";

	private final ControleDeAcesso controle = new ControleDeAcesso();

	@AfterEach
	void limparContexto() {
		SecurityContextHolder.clearContext();
	}

	private static void autenticar(String role, String documento) {
		Jwt.Builder jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("usuario");
		if (documento != null) {
			jwt.claim(ControleDeAcesso.CLAIM_DOCUMENTO, documento);
		}
		SecurityContextHolder.getContext().setAuthentication(
				new JwtAuthenticationToken(jwt.build(), AuthorityUtils.createAuthorityList(role)));
	}

	@Test
	void adminAcessaRecursoDeQualquerCliente() {
		autenticar("ROLE_ADMIN", null);

		assertThatNoException().isThrownBy(() -> controle.verificarDono(CPF_DE_OUTRO_CLIENTE));
	}

	@Test
	void clienteAcessaOProprioRecursoComOuSemMascara() {
		autenticar("ROLE_CLIENTE", CPF_DO_CLIENTE);

		assertThatNoException().isThrownBy(() -> controle.verificarDono(CPF_DO_CLIENTE));
		assertThatNoException().isThrownBy(() -> controle.verificarDono("529.982.247-25"));
	}

	@Test
	void clienteNaoAcessaRecursoDeOutroCliente() {
		autenticar("ROLE_CLIENTE", CPF_DO_CLIENTE);

		assertThatThrownBy(() -> controle.verificarDono(CPF_DE_OUTRO_CLIENTE))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("outro cliente");
	}

	@Test
	void recursoSemDonoEhNegadoAoCliente() {
		autenticar("ROLE_CLIENTE", CPF_DO_CLIENTE);

		assertThatThrownBy(() -> controle.verificarDono(null)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void clienteSemDocumentoNoTokenEhNegado() {
		autenticar("ROLE_CLIENTE", null);

		assertThatThrownBy(() -> controle.verificarDono(CPF_DO_CLIENTE)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void documentoCertoSemPerfilDeClienteEhNegado() {
		autenticar("ROLE_OUTRO", CPF_DO_CLIENTE);

		assertThatThrownBy(() -> controle.verificarDono(CPF_DO_CLIENTE)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void semAutenticacaoEhNegado() {
		assertThatThrownBy(() -> controle.verificarDono(CPF_DO_CLIENTE)).isInstanceOf(AccessDeniedException.class);
	}
}

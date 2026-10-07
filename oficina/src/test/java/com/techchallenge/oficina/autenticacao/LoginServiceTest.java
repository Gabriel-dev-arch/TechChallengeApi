package com.techchallenge.oficina.autenticacao;

import com.techchallenge.oficina.autenticacao.dominio.CredenciaisInvalidasException;
import com.techchallenge.oficina.autenticacao.dominio.GeradorDeToken;
import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.autenticacao.dominio.UsuarioRepository;
import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import com.techchallenge.oficina.autenticacao.login.LoginRequest;
import com.techchallenge.oficina.autenticacao.login.LoginService;
import com.techchallenge.oficina.autenticacao.login.TokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

	private static final String HASH = "hash-bcrypt-do-admin";
	private static final String MENSAGEM = "Usuário ou senha inválidos";

	@Mock
	UsuarioRepository repository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	GeradorDeToken geradorDeToken;

	@InjectMocks
	LoginService service;

	@Test
	void credenciaisCorretasDevolvemToken() {
		Usuario admin = new Usuario("admin", HASH, Perfil.ADMIN);
		Instant expiraEm = Instant.now().plus(60, ChronoUnit.MINUTES);
		Jwt jwt = Jwt.withTokenValue("token-assinado").header("alg", "HS256").subject("admin")
				.issuedAt(Instant.now()).expiresAt(expiraEm).build();
		when(repository.findByUsername("admin")).thenReturn(Optional.of(admin));
		when(passwordEncoder.matches("admin123", HASH)).thenReturn(true);
		when(geradorDeToken.gerar(admin)).thenReturn(jwt);

		TokenResponse resposta = service.login(new LoginRequest("admin", "admin123"));

		assertThat(resposta.accessToken()).isEqualTo("token-assinado");
		assertThat(resposta.tipo()).isEqualTo("Bearer");
		assertThat(resposta.expiraEm().toInstant()).isEqualTo(expiraEm);
		assertThat(resposta.expiraEm().getOffset()).isEqualTo(ZoneOffset.ofHours(-3));
	}

	@Test
	void senhaErradaLancaCredenciaisInvalidas() {
		when(repository.findByUsername("admin")).thenReturn(Optional.of(new Usuario("admin", HASH, Perfil.ADMIN)));
		when(passwordEncoder.matches("errada", HASH)).thenReturn(false);

		assertThatThrownBy(() -> service.login(new LoginRequest("admin", "errada")))
				.isInstanceOf(CredenciaisInvalidasException.class).hasMessage(MENSAGEM);
		verify(geradorDeToken, never()).gerar(any());
	}

	@Test
	void usuarioInexistenteLancaAMesmaExcecaoEConfereSenhaMesmoAssim() {
		when(repository.findByUsername("fantasma")).thenReturn(Optional.empty());

		Throwable erro = catchThrowable(() -> service.login(new LoginRequest("fantasma", "admin123")));

		assertThat(erro).isInstanceOf(CredenciaisInvalidasException.class).hasMessage(MENSAGEM);
		// a senha é conferida contra um hash fictício para o tempo de resposta não revelar que o usuário não existe
		verify(passwordEncoder).matches(any(), any());
		verify(geradorDeToken, never()).gerar(any());
	}

	@Test
	void usuarioInativoLancaCredenciaisInvalidasMesmoComSenhaCorreta() {
		Usuario inativo = new Usuario("admin", HASH, Perfil.ADMIN);
		inativo.desativar();
		when(repository.findByUsername("admin")).thenReturn(Optional.of(inativo));

		assertThatThrownBy(() -> service.login(new LoginRequest("admin", "admin123")))
				.isInstanceOf(CredenciaisInvalidasException.class).hasMessage(MENSAGEM);
		verify(passwordEncoder, never()).matches("admin123", HASH);
		verify(geradorDeToken, never()).gerar(any());
	}
}

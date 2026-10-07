package com.techchallenge.oficina.config;

import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.autenticacao.dominio.UsuarioRepository;
import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministradorInicialRunnerTest {

	@Mock
	UsuarioRepository repository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Test
	void criaAdministradorComSenhaCodificadaQuandoNaoHaUsuarios() {
		when(repository.count()).thenReturn(0L);
		when(passwordEncoder.encode("admin123")).thenReturn("hash-bcrypt");

		new AdministradorInicialRunner(repository, passwordEncoder, "admin", "admin123").run(null);

		ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
		verify(repository).save(salvo.capture());
		assertThat(salvo.getValue().getUsername()).isEqualTo("admin");
		assertThat(salvo.getValue().getSenhaHash()).isEqualTo("hash-bcrypt");
		assertThat(salvo.getValue().getPerfil()).isEqualTo(Perfil.ADMIN);
		assertThat(salvo.getValue().isAtivo()).isTrue();
	}

	@Test
	void naoCriaNadaQuandoJaExistemUsuarios() {
		when(repository.count()).thenReturn(1L);

		new AdministradorInicialRunner(repository, passwordEncoder, "admin", "admin123").run(null);

		verify(repository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void naoCriaNadaSemAsVariaveisDeAmbiente() {
		when(repository.count()).thenReturn(0L);

		new AdministradorInicialRunner(repository, passwordEncoder, "", "").run(null);
		new AdministradorInicialRunner(repository, passwordEncoder, "admin", " ").run(null);

		verify(repository, never()).save(any());
	}
}

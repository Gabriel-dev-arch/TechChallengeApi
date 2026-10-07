package com.techchallenge.oficina.autenticacao.login;

import com.techchallenge.oficina.autenticacao.dominio.CredenciaisInvalidasException;
import com.techchallenge.oficina.autenticacao.dominio.GeradorDeToken;
import com.techchallenge.oficina.autenticacao.dominio.UsuarioRepository;
import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {

	private final UsuarioRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final GeradorDeToken geradorDeToken;

	/** Hash descartável, conferido quando o usuário não existe, para o tempo de resposta não revelar isso. */
	private final String hashFicticio;

	public LoginService(UsuarioRepository repository, PasswordEncoder passwordEncoder, GeradorDeToken geradorDeToken) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.geradorDeToken = geradorDeToken;
		this.hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
	}

	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		Optional<Usuario> usuario = repository.findByUsername(request.username()).filter(Usuario::isAtivo);
		String hash = usuario.map(Usuario::getSenhaHash).orElse(hashFicticio);
		boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);
		if (usuario.isEmpty() || !senhaConfere) {
			throw new CredenciaisInvalidasException();
		}
		return TokenResponse.from(geradorDeToken.gerar(usuario.get()));
	}
}

package com.techchallenge.oficina.autenticacao.login;

import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Service
public class LoginService {

	private final JwtEncoder jwtEncoder;
	private final List<Usuario> usuarios;
	private final Duration expiracao;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder passwordEncoder;

	public LoginService(JwtEncoder jwtEncoder, ClienteRepository clienteRepository, PasswordEncoder passwordEncoder,
						@Value("${seguranca.admin.username}") String adminUsername,
						@Value("${seguranca.admin.password}") String adminPassword,
						@Value("${seguranca.cliente.username}") String clienteUsername,
						@Value("${seguranca.cliente.password}") String clientePassword,
						@Value("${seguranca.cliente.documento}") String clienteDocumento,
						@Value("${seguranca.jwt.expiracao-minutos}") long expiracaoMinutos) {
		if (!CpfCnpjValidator.valido(clienteDocumento)) {
			throw new IllegalStateException("CLIENTE_DOCUMENTO deve ser um CPF ou CNPJ válido");
		}
		this.jwtEncoder = jwtEncoder;
		this.clienteRepository = clienteRepository;
		this.passwordEncoder = passwordEncoder;
		this.usuarios = List.of(
				new Usuario(adminUsername, adminPassword, Perfil.ADMIN, null),
				new Usuario(clienteUsername, clientePassword, Perfil.CLIENTE, CpfCnpjValidator.normalizar(clienteDocumento)));
		this.expiracao = Duration.ofMinutes(expiracaoMinutos);
	}

	public LoginResponse autenticar(LoginRequest request) {
		Usuario usuario = buscarUsuario(request);
		Instant agora = Instant.now();
		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
				.subject(usuario.username())
				.issuedAt(agora)
				.expiresAt(agora.plus(expiracao))
				.claim(Perfil.CLAIM_JWT, List.of(usuario.perfil().name()));
		if (usuario.documento() != null) {
			claims.claim(ControleDeAcesso.CLAIM_DOCUMENTO, usuario.documento());
		}
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
		return new LoginResponse(token, "Bearer", expiracao.toSeconds());
	}

	private Usuario buscarUsuario(LoginRequest request) {
		if (CpfCnpjValidator.valido(request.username())) {
			Cliente cliente = clienteRepository.findByDocumento(CpfCnpjValidator.normalizar(request.username()))
					.orElseThrow(CredenciaisInvalidasException::new);
			if (cliente.getSenhaHash() == null || request.password().getBytes(StandardCharsets.UTF_8).length > 72
					|| !passwordEncoder.matches(request.password(), cliente.getSenhaHash())) {
				throw new CredenciaisInvalidasException();
			}
			return new Usuario(cliente.getDocumento(), null, Perfil.CLIENTE, cliente.getDocumento());
		}
		Usuario usuario = usuarios.stream()
				.filter(u -> u.username().equals(request.username()) && u.password().equals(request.password()))
				.findFirst()
				.orElseThrow(CredenciaisInvalidasException::new);
		if (usuario.perfil() == Perfil.CLIENTE && clienteRepository.findByDocumento(usuario.documento())
				.filter(cliente -> cliente.getSenhaHash() != null).isPresent()) {
			throw new CredenciaisInvalidasException();
		}
		return usuario;
	}

	private record Usuario(String username, String password, Perfil perfil, String documento) {
	}
}

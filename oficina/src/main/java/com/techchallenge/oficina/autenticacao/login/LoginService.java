package com.techchallenge.oficina.autenticacao.login;

import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
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

@Service
public class LoginService {

	private final JwtEncoder jwtEncoder;
	private final List<Usuario> usuarios;
	private final Duration expiracao;

	public LoginService(JwtEncoder jwtEncoder,
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
		this.usuarios = List.of(
				new Usuario(adminUsername, adminPassword, Perfil.ADMIN, null),
				new Usuario(clienteUsername, clientePassword, Perfil.CLIENTE, CpfCnpjValidator.normalizar(clienteDocumento)));
		this.expiracao = Duration.ofMinutes(expiracaoMinutos);
	}

	public LoginResponse autenticar(LoginRequest request) {
		Usuario usuario = usuarios.stream()
				.filter(u -> u.username().equals(request.username()) && u.password().equals(request.password()))
				.findFirst()
				.orElseThrow(CredenciaisInvalidasException::new);
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

	private record Usuario(String username, String password, Perfil perfil, String documento) {
	}
}

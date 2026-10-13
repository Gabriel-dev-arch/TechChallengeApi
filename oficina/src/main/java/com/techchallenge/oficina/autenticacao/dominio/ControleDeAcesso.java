package com.techchallenge.oficina.autenticacao.dominio;

import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Autorização por dono do recurso, contra BOLA (OWASP API Security Top 10 2023 - API1: Broken Object Level
 * Authorization).
 *
 * <p>Ter um token válido não basta: um usuário CLIENTE só pode ver ou alterar recursos do próprio CPF/CNPJ,
 * que entra no token (claim {@code documento}) no login. ADMIN acessa qualquer recurso.
 * Todo endpoint liberado ao perfil CLIENTE deve chamar {@link #verificarDono(String)} com o documento do dono
 * do recurso antes de devolvê-lo ou alterá-lo (ex.: a OS consultada ou aprovada pelo cliente).
 * Na dúvida (sem autenticação, sem perfil, sem documento), o acesso é negado.
 */
@Component
public class ControleDeAcesso {

	public static final String CLAIM_DOCUMENTO = "documento";

	/** @throws AccessDeniedException (HTTP 403) se o usuário não for ADMIN nem o dono do recurso */
	public void verificarDono(String documentoDoDono) {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null) {
			throw new AccessDeniedException("Acesso negado");
		}
		if (temPerfil(autenticacao, Perfil.ADMIN)) {
			return;
		}
		String dono = CpfCnpjValidator.normalizar(documentoDoDono);
		boolean ehDono = temPerfil(autenticacao, Perfil.CLIENTE)
				&& autenticacao.getPrincipal() instanceof Jwt jwt
				&& dono != null
				&& dono.equals(jwt.getClaimAsString(CLAIM_DOCUMENTO));
		if (!ehDono) {
			throw new AccessDeniedException("Acesso negado: o recurso pertence a outro cliente");
		}
	}

	private static boolean temPerfil(Authentication autenticacao, Perfil perfil) {
		String role = "ROLE_" + perfil.name();
		return autenticacao.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
	}
}

package com.techchallenge.oficina.autenticacao.dominio;

import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Evita BOLA (OWASP API1): o CLIENTE só acessa recursos do próprio documento; ADMIN acessa tudo. */
@Component
public class ControleDeAcesso {

	public static final String CLAIM_DOCUMENTO = "documento";

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

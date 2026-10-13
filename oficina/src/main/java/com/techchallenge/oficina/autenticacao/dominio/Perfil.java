package com.techchallenge.oficina.autenticacao.dominio;

/** Perfis de acesso. Vão no claim {@code roles} do token e viram ROLE_ADMIN / ROLE_CLIENTE no Spring Security. */
public enum Perfil {

	/** Funcionário da oficina: acessa todas as APIs. */
	ADMIN,

	/** Dono do veículo: acessa só as rotas liberadas ao cliente e só os próprios recursos (ver {@link ControleDeAcesso}). */
	CLIENTE;

	public static final String CLAIM_JWT = "roles";
}

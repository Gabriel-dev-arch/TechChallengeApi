package com.techchallenge.oficina.autenticacao.dominio;

/** Perfil de acesso do usuário. Vai na claim {@code roles} do token e vira a authority {@code ROLE_<perfil>}. */
public enum Perfil {
	ADMIN
}

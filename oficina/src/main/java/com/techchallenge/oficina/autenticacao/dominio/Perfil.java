package com.techchallenge.oficina.autenticacao.dominio;

public enum Perfil {

	ADMIN,
	CLIENTE;

	public static final String CLAIM_JWT = "roles";
}

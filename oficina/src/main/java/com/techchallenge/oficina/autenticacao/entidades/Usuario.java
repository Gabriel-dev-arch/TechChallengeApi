package com.techchallenge.oficina.autenticacao.entidades;

import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "usuarios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario extends BaseEntity {

	private String username;

	/** Hash BCrypt: a entidade recebe a senha já codificada e nunca conhece a senha em texto puro. */
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	private Perfil perfil;

	private boolean ativo;

	public Usuario(String username, String senhaHash, Perfil perfil) {
		this.username = username;
		this.senhaHash = senhaHash;
		this.perfil = perfil;
		this.ativo = true;
	}

	public void desativar() {
		this.ativo = false;
	}
}

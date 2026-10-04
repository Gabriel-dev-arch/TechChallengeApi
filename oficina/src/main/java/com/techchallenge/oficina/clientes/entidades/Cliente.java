package com.techchallenge.oficina.clientes.entidades;

import com.techchallenge.oficina.shared.persistence.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "clientes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cliente extends BaseEntity {

	private String firstName;

	private String lastName;

	private String fullName;

	private String email;

	private String documento;

	private String telefone;

	public Cliente(String firstName, String lastName, String email, String documento, String telefone) {
		atualizar(firstName, lastName, email, documento, telefone);
	}

	public void atualizar(String firstName, String lastName, String email, String documento, String telefone) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.fullName = firstName + " " + lastName;
		this.email = email;
		this.documento = documento;
		this.telefone = telefone;
	}
}

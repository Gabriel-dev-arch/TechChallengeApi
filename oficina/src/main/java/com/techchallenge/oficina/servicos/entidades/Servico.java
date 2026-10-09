package com.techchallenge.oficina.servicos.entidades;

import java.math.BigDecimal;

import com.techchallenge.oficina.shared.persistence.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "servicos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Servico extends BaseEntity {
	
	private String descricao;
	private BigDecimal valor;
	
	public Servico(String descricao, BigDecimal valor) {
		super();
		this.descricao = descricao;
		this.valor = valor;
	}
	
	public void atualizar(String descricao, BigDecimal valor) {

		this.descricao = descricao;
		this.valor = valor;
	}
}

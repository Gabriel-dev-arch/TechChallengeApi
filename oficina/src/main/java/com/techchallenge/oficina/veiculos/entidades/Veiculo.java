package com.techchallenge.oficina.veiculos.entidades;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "veiculos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Veiculo extends BaseEntity {

	private String placa;

	private String marca;

	private String modelo;

	private Integer ano;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	public Veiculo(String placa, String marca, String modelo, Integer ano, Cliente cliente) {
		atualizar(placa, marca, modelo, ano);
		this.cliente = cliente;
	}

	public void atualizar(String placa, String marca, String modelo, Integer ano) {
		this.placa = placa;
		this.marca = marca;
		this.modelo = modelo;
		this.ano = ano;
	}
}

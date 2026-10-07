package com.techchallenge.oficina.veiculos.entidades;

import com.techchallenge.oficina.shared.persistence.BaseEntity;
import jakarta.persistence.Entity;
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

	public Veiculo(String placa, String marca, String modelo, Integer ano) {
		atualizar(placa, marca, modelo, ano);
	}

	public void atualizar(String placa, String marca, String modelo, Integer ano) {
		this.placa = placa;
		this.marca = marca;
		this.modelo = modelo;
		this.ano = ano;
	}
}

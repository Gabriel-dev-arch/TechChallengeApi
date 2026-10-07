package com.techchallenge.oficina.veiculos.dominio;

public class PlacaJaCadastradaException extends RuntimeException {

	public PlacaJaCadastradaException(String placa) {
		super("Já existe um veículo cadastrado com a placa " + placa);
	}
}

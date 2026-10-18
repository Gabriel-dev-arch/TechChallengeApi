package com.techchallenge.oficina.veiculos.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

public class PlacaJaCadastradaException extends ConflitoException {

	public PlacaJaCadastradaException(String placa) {
		super("Já existe um veículo cadastrado com a placa " + placa);
	}
}

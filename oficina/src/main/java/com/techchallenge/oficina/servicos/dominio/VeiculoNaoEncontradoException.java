package com.techchallenge.oficina.servicos.dominio;

public class VeiculoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

		
	public VeiculoNaoEncontradoException(String placa) {
		super("Veiculo não encontrado: " + placa);
	}
}


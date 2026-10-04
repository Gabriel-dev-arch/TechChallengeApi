package com.techchallenge.oficina.clientes.dominio;

public class DocumentoJaCadastradoException extends RuntimeException {

	public DocumentoJaCadastradoException(String documento) {
		super("Já existe um cliente cadastrado com o documento " + documento);
	}
}

package com.techchallenge.oficina.clientes.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

public class DocumentoJaCadastradoException extends ConflitoException {

	public DocumentoJaCadastradoException(String documento) {
		super("Já existe um cliente cadastrado com o documento " + documento);
	}
}

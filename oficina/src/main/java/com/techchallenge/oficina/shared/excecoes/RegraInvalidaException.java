package com.techchallenge.oficina.shared.excecoes;

/**
 * Dado que viola uma regra de domínio (quantidade ou preço inválidos...). Vira HTTP 400.
 * Exceções de domínio dos módulos devem estender esta classe; o GlobalExceptionHandler
 * já a trata, então nenhum módulo novo precisa alterar o handler.
 */
public abstract class RegraInvalidaException extends RuntimeException {

	protected RegraInvalidaException(String mensagem) {
		super(mensagem);
	}
}

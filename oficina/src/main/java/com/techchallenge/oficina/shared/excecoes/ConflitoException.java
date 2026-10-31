package com.techchallenge.oficina.shared.excecoes;

/**
 * Operação válida, mas que conflita com o estado atual dos dados (duplicidade, estoque insuficiente, item inativo...). Vira HTTP 409.
 * Exceções de domínio dos módulos devem estender esta classe; o GlobalExceptionHandler
 * já a trata, então nenhum módulo novo precisa alterar o handler.
 */
public abstract class ConflitoException extends RuntimeException {

	protected ConflitoException(String mensagem) {
		super(mensagem);
	}
}

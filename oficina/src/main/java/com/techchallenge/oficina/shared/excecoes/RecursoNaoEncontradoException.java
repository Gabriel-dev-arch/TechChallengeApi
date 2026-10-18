package com.techchallenge.oficina.shared.excecoes;

/**
 * Recurso inexistente (ex.: cliente, veículo ou peça/insumo não encontrado). Vira HTTP 404.
 * Exceções de domínio dos módulos devem estender esta classe; o GlobalExceptionHandler
 * já a trata, então nenhum módulo novo precisa alterar o handler.
 */
public abstract class RecursoNaoEncontradoException extends RuntimeException {

	protected RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}
}

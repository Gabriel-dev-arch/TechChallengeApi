package com.techchallenge.oficina.shared.excecoes;

/**
 * Falha de autenticação (ex.: usuário ou senha inválidos no login). Vira HTTP 401.
 * Exceções de domínio dos módulos devem estender esta classe; o GlobalExceptionHandler
 * já a trata, então nenhum módulo novo precisa alterar o handler.
 */
public abstract class NaoAutenticadoException extends RuntimeException {

	protected NaoAutenticadoException(String mensagem) {
		super(mensagem);
	}
}

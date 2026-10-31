package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.shared.excecoes.RegraInvalidaException;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Status de OS desconhecido em um filtro. Vira HTTP 400. */
public class StatusInvalidoException extends RegraInvalidaException {
	public StatusInvalidoException(String status) {
		super("Status inválido: '" + status + "'. Valores aceitos: "
				+ Arrays.stream(StatusOS.values()).map(Enum::name).collect(Collectors.joining(", ")));
	}
}

package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.ordem_servico.dominio.exception.StatusInvalidoException;

public enum StatusOS {

	RECEBIDA("Recebida"),
	EM_DIAGNOSTICO("Em diagnóstico"),
	AGUARDANDO_APROVACAO("Aguardando aprovação"),
	APROVADA("Aprovada"),
	NEGADA("negada"),
	EM_EXECUCAO("Em execução"),
	FINALIZADA("Finalizada"),
	ENTREGUE("Entregue");

	private final String descricao;

	StatusOS(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	/** Converte o valor de um filtro (sem diferenciar maiúsculas); valor desconhecido vira HTTP 400. */
	public static StatusOS de(String valor) {
		if (valor != null) {
			for (StatusOS status : values()) {
				if (status.name().equalsIgnoreCase(valor.strip())) {
					return status;
				}
			}
		}
		throw new StatusInvalidoException(valor);
	}
}
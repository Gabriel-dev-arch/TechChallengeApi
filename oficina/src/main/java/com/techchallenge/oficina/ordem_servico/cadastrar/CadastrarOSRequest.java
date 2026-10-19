package com.techchallenge.oficina.ordem_servico.cadastrar;

import java.util.List;
import java.util.UUID;

public record CadastrarOSRequest(
		List<UUID> servicos,
		List<ItemQuantidade> itens,
		String diagnostico,
		String placa) {
}

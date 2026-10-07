package com.techchallenge.oficina.servicos.cadastrar;

import jakarta.validation.constraints.NotBlank;

public record CadastrarServicoRequest(
		@NotBlank String placa,
		@NotBlank String cpf
		) {

}

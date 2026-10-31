package com.techchallenge.oficina.servicos.atualizar;

import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Servicos", description = "Atualizar Servicos")
@RestController
@RequestMapping("/servicos")
public class AtualizarServicoController {

	private final AtualizarServicoService service;

	public AtualizarServicoController(AtualizarServicoService service) {
		this.service = service;
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar servico",
			description = "Atualizar servico"
	)
	public ServicoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarServicoRequest request) {
		return service.atualizar(id, request);
	}
}

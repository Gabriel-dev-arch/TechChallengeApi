package com.techchallenge.oficina.servicos.remover;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Servicos", description = "Remover Servicos")
@RestController
@RequestMapping("/servicos")
public class RemoverServicoController {

	private final RemoverServicoService service;

	public RemoverServicoController(RemoverServicoService service) {
		this.service = service;
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remover servico", description = "Remover servico")
	public void remover(@PathVariable UUID id) {
		service.remover(id);
	}
}

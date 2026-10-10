package com.techchallenge.oficina.veiculos.remover;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Veiculos", description = "Remover Veiculos")
@RestController
@RequestMapping("/veiculos")
public class RemoverVeiculoController {

	private final RemoverVeiculoService service;

	public RemoverVeiculoController(RemoverVeiculoService service) {
		this.service = service;
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
			summary = "Remover Veiculos",
			description = "Remover Veiculos"
	)
	public void remover(@PathVariable UUID id) {
		service.remover(id);
	}
}

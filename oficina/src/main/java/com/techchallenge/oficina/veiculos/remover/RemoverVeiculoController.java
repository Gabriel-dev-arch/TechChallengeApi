package com.techchallenge.oficina.veiculos.remover;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
	public void remover(@PathVariable UUID id) {
		service.remover(id);
	}
}

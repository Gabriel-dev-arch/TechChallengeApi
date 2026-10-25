package com.techchallenge.oficina.clientes.remover;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Clientes", description = "Remover Clientes")
@RestController
@RequestMapping("/clientes")
public class RemoverClienteController {

	private final RemoverClienteService service;

	public RemoverClienteController(RemoverClienteService service) {
		this.service = service;
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remover Cliente", description = "Remover Cliente")
	public void remover(@PathVariable UUID id) {
		service.remover(id);
	}
}

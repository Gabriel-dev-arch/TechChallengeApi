package com.techchallenge.oficina.clientes.atualizar;

import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Clientes", description = "Atualizar Clientes")
@RestController
@RequestMapping("/clientes")
public class AtualizarClienteController {

	private final AtualizarClienteService service;

	public AtualizarClienteController(AtualizarClienteService service) {
		this.service = service;
	}

	@PutMapping("/{id}")
	public ClienteResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarClienteRequest request) {
		return service.atualizar(id, request);
	}
}

package com.techchallenge.oficina.servicos.consultar;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.techchallenge.oficina.servicos.dominio.ServicoResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Servicos", description = "Consultar Servicos")
@RestController
@RequestMapping("/servicos")
public class ConsultarServicoController {

	private final ConsultarServicoService service;

	public ConsultarServicoController(ConsultarServicoService service) {
		this.service = service;
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca servicos pelo ID", description = "Busca servicos pelo ID")
	public ResponseEntity<ServicoResponse> consultar(@PathVariable String id) {
		ServicoResponse servico = service.listar(UUID.fromString(id));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(servico.id());
		return ResponseEntity.created(location).body(servico);
	}

	@GetMapping
	@Operation(summary = "Listar servicos", description = "Lista servicos")
	public ResponseEntity<List<ServicoResponse>> listarTodos() {
		return ResponseEntity.ok(service.listarTodos());
	}
}

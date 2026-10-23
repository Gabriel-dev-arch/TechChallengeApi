package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Ordem de Serviço", description = "Cadastra Ordem de serviço")
@RestController
@RequestMapping("/ordens-servico")
public class CadastrarOSController {

	private final CadastrarOSService service;

	public CadastrarOSController(CadastrarOSService service) {
		this.service = service;
	}

	@PostMapping
	@Operation(summary = "Cadastra ordem de serviço", description = "Cadastra ordem de serviço")
	public ResponseEntity<OrdemServicoResponse> cadastrar(@Valid @RequestBody CadastrarOSRequest request) {
		OrdemServicoResponse servicoResponse = service.cadastrar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(request.placa());
		return ResponseEntity.created(location).body(servicoResponse);
	}

	@PostMapping("/{id}/servicos")
	@Operation(summary = "Inclui serviços a OS", description = "Inclui serviços a OS")
	public ResponseEntity<OrdemServicoResponse> incluiServicoOS(@Valid @RequestBody List<ServicosRequest> request,
																 @PathVariable UUID id) {

		OrdemServicoResponse servicoResponse = service.incluiServicosOS(id, request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(id);
		return ResponseEntity.created(location).body(servicoResponse);
	}

	@PostMapping("/{id}/pecas")
	@Operation(summary = "Inclui peças ou insumos a OS", description = "Inclui peças ou insumos a OS")
	public ResponseEntity<OrdemServicoResponse> incluiPecasInsumosOS(@Valid @RequestBody List<ItemQuantidadeRequest> request,
																 @PathVariable UUID id) {

		OrdemServicoResponse servicoResponse = service.incluiPecasInsumosOS(id, request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(id);
		return ResponseEntity.created(location).body(servicoResponse);
	}
}

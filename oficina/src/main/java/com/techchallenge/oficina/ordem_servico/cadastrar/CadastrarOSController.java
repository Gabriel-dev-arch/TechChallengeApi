package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.cadastrar.request.CadastrarOSRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidadeRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ServicosRequest;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
	public ResponseEntity<OrdemServicoResponse> incluiServicoOS(@NotEmpty @RequestBody List<ServicosRequest> request, @PathVariable("id") UUID id) {

		return ResponseEntity.ok(service.incluiServicosOS(id, request));
	}

	@PostMapping("/{id}/pecas")
	@Operation(summary = "Inclui peças ou insumos a OS", description = "Inclui peças ou insumos a OS")
	public ResponseEntity<OrdemServicoResponse> incluiPecasInsumosOS(@NotEmpty @RequestBody List<ItemQuantidadeRequest> request,
																 	@PathVariable("id") UUID id) {

		return ResponseEntity.ok(service.incluiPecasInsumosOS(id, request));
	}

	@PostMapping("/{id}/iniciar-diagnostico")
	@Operation(summary = "Inicia diagnóstico do veículo", description = "Inicia diagnóstico do veículo")
	public ResponseEntity<OrdemServicoResponse> iniciaDiagnostico(@PathVariable("id") UUID id) {

		return ResponseEntity.ok(service.iniciaDiagnostico(id));
	}


	@PostMapping("/{id}/orcamento")
	@Operation(summary = "Realiza o orçamento", description = "Realiza o orçamento")
	public ResponseEntity<OrdemServicoResponse> iniciaOrcamento(@PathVariable("id") UUID id) {

		return ResponseEntity.ok(service.cadastrarOrcamento(id));
	}

	@PostMapping("/{id}/iniciar-execucao")
	@Operation(summary = "Realiza a exeecução do serviço", description = "Realiza a exeecução do serviço")
	public ResponseEntity<OrdemServicoResponse> iniciaExecucao(@PathVariable("id") UUID id) {

		return ResponseEntity.ok(service.iniciaExecucao(id));
	}

	@PostMapping("/{id}/finalizar")
	@Operation(summary = "Finaliza a exeecução do serviço", description = "Finaliza a exeecução do serviço")
	public ResponseEntity<OrdemServicoResponse> finalizaExecucao(@PathVariable("id") UUID id) {

		return ResponseEntity.ok(service.finalizaExecucao(id));
	}

	@PostMapping("/{id}/entregar")
	@Operation(summary = "Finaliza a exeecução do serviço", description = "Finaliza a exeecução do serviço")
	public ResponseEntity<OrdemServicoResponse> entregarVeiculo(@PathVariable("id") UUID id) {

		return ResponseEntity.ok(service.entregaVeiculo(id));
	}
}

package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Ordem de Serviço", description = "Cadastra Ordem de serviço")
@RestController
@RequestMapping("/os")
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
}

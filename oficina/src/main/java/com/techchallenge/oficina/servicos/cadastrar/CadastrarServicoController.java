package com.techchallenge.oficina.servicos.cadastrar;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.techchallenge.oficina.servicos.dominio.ServicoResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Servicos", description = "Criar Servicos")
@RestController
@RequestMapping("/servicos")
public class CadastrarServicoController {

	private final CadastrarServicoService service;

	public CadastrarServicoController(CadastrarServicoService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ServicoResponse> cadastrar(@Valid @RequestBody CadastrarServicoRequest request) {
		ServicoResponse servico = service.cadastrar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(1);
		return ResponseEntity.created(location).body(servico);
	}
}

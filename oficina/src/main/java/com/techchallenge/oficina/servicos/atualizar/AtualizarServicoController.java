package com.techchallenge.oficina.servicos.atualizar;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techchallenge.oficina.servicos.dominio.ServicoResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Servicos", description = "Atualizar Servicos")
@RestController
@RequestMapping("/servicos")
public class AtualizarServicoController {

	private final AtualizarServicoService service;

	public AtualizarServicoController(AtualizarServicoService service) {
		this.service = service;
	}

	@PutMapping("/{id}")
	public ServicoResponse cadastrar(@PathVariable UUID id, @Valid @RequestBody AtualizarServicoRequest request) {
		return service.atualizar(id, request);
	}
}

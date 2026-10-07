package com.techchallenge.oficina.veiculos.atualizar;

import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Veiculos", description = "Atualizar Veiculos")
@RestController
@RequestMapping("/veiculos")
public class AtualizarVeiculoController {

	private final AtualizarVeiculoService service;

	public AtualizarVeiculoController(AtualizarVeiculoService service) {
		this.service = service;
	}

	@PutMapping("/{id}")
	public VeiculoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarVeiculoRequest request) {
		return service.atualizar(id, request);
	}
}

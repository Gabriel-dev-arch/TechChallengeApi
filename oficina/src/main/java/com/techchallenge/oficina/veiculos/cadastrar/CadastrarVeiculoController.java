package com.techchallenge.oficina.veiculos.cadastrar;

import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Veiculos", description = "Criar Veiculos")
@RestController
@RequestMapping("/veiculos")
public class CadastrarVeiculoController {

	private final CadastrarVeiculoService service;

	public CadastrarVeiculoController(CadastrarVeiculoService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody CadastrarVeiculoRequest request) {
		VeiculoResponse veiculo = service.cadastrar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(veiculo.id());
		return ResponseEntity.created(location).body(veiculo);
	}
}

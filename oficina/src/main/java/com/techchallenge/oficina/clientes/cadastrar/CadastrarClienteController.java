package com.techchallenge.oficina.clientes.cadastrar;

import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Criar Clientes")
@RestController
@RequestMapping("/clientes")
public class CadastrarClienteController {

	private final CadastrarClienteService service;

	public CadastrarClienteController(CadastrarClienteService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody CadastrarClienteRequest request) {
		ClienteResponse cliente = service.cadastrar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(cliente.id());
		return ResponseEntity.created(location).body(cliente);
	}
}

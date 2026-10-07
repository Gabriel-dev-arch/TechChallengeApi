package com.techchallenge.oficina.clientes.consultar;

import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Clientes", description = "Consultar Clientes")
@RestController
@RequestMapping("/clientes")
public class ConsultarClientesController {

	private final ConsultarClientesService service;

	public ConsultarClientesController(ConsultarClientesService service) {
		this.service = service;
	}

	@GetMapping
	public PagedModel<ClienteResponse> listar(@RequestParam(required = false) String documento,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("fullName"));
		return new PagedModel<>(service.listar(documento, pageable));
	}

	@GetMapping("/{id}")
	public ClienteResponse buscar(@PathVariable UUID id) {
		return service.buscar(id);
	}
}

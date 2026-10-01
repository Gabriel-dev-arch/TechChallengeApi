package com.techchallenge.oficina.clientes.consultar;

import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Consultar Clientes")
@RestController
@RequestMapping("/clientes")
public class ConsultarClientesController {

	private final ConsultarClientesService service;

	public ConsultarClientesController(ConsultarClientesService service) {
		this.service = service;
	}

	@GetMapping
	public PagedModel<ClienteResponse> listar(@RequestParam(required = false) String documento,
			@PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
		return new PagedModel<>(service.listar(documento, pageable));
	}

	@GetMapping("/{id}")
	public ClienteResponse buscar(@PathVariable UUID id) {
		return service.buscar(id);
	}
}

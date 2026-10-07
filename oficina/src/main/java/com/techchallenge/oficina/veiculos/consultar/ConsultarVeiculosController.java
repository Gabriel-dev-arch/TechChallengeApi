package com.techchallenge.oficina.veiculos.consultar;

import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
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

@Tag(name = "Veiculos", description = "Consultar Veiculos")
@RestController
@RequestMapping("/veiculos")
public class ConsultarVeiculosController {

	private final ConsultarVeiculosService service;

	public ConsultarVeiculosController(ConsultarVeiculosService service) {
		this.service = service;
	}

	@GetMapping
	public PagedModel<VeiculoResponse> listar(@RequestParam(required = false) String placa,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("placa"));
		return new PagedModel<>(service.listar(placa, pageable));
	}

	@GetMapping("/{id}")
	public VeiculoResponse buscar(@PathVariable UUID id) {
		return service.buscar(id);
	}
}

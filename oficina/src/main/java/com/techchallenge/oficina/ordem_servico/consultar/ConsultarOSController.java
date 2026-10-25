package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Ordem de Serviço", description = "Consultar Ordem de Sefrviço")
@RestController
@RequestMapping("/ordens-servico")
public class ConsultarOSController {

	private final ConsultarOSService service;

	public ConsultarOSController(ConsultarOSService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Buscar Ordens de Serviço pelo Status", description = "Buscar Ordens de Serviço pelo Status")
	public PagedModel<OrdemServicoResponse> listar(@RequestParam(defaultValue = "0") @Min(0) int page,
												   @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
												   @RequestParam(required = true) String status) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
		return new PagedModel<>(service.listarOrdemServicoEmAberto(StatusOS.valueOf(status), pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar Ordem de Serviço pelo id", description = "Buscar Ordem de Serviço pelo id")
	public OrdemServicoResponse buscarOSPeloID(@PathVariable UUID id) {
		return service.buscarOSPeloID(id);
	}
}

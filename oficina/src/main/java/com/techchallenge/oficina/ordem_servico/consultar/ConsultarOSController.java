package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Ordem de Serviço", description = "Consultar Ordem de Sefrviço")
@RestController
@RequestMapping("/os")
public class ConsultarOSController {

	private final ConsultarOSService service;

	public ConsultarOSController(ConsultarOSService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Listar Ordem de Serviço", description = "Listar Ordem de Serviço")
	public List<OrdemServicoResponse> listar(@RequestParam(required = false) UUID id) {
		return service.listar(id);
	}

	@GetMapping("/{placa}")
	@Operation(summary = "Buscar Ordem de Serviço pela placa", description = "Buscar Ordem de Serviço pela placa")
	public OrdemServicoResponse buscar(@PathVariable String placa) {
		return service.buscar(placa);
	}
}

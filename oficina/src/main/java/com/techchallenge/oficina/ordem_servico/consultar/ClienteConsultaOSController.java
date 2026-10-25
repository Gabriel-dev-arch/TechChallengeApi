package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.ordem_servico.cadastrar.CadastrarOSService;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.CadastrarOSRequest;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Ordem de Serviço", description = "Cadastra Ordem de serviço")
@RestController
@RequestMapping("/public/os")
public class ClienteConsultaOSController {

    private final ConsultarOSService service;

    public ClienteConsultaOSController(ConsultarOSService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Acompanhar status da OS", description = "Acompanhar status da OS")
    public ResponseEntity<OrdemServicoResponse> cadastrar(@PathVariable UUID id) {
        OrdemServicoResponse servicoResponse = service.consultarStatusOS(id);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(id);
        return ResponseEntity.ok(servicoResponse);
    }
}

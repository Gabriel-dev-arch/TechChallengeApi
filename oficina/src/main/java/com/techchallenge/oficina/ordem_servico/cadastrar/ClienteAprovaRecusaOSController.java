package com.techchallenge.oficina.ordem_servico.cadastrar;

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
public class ClienteAprovaRecusaOSController {

    private final ClienteAprovaRecusaOSService service;

    public ClienteAprovaRecusaOSController(ClienteAprovaRecusaOSService service) {
        this.service = service;
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Cadastra ordem de serviço", description = "Cadastra ordem de serviço")
    public ResponseEntity aprovar(@PathVariable("id") UUID id) {

        service.aprovaOS(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/recusar")
    @Operation(summary = "Cadastra ordem de serviço", description = "Cadastra ordem de serviço")
    public ResponseEntity recusar(@PathVariable("id") UUID id) {

        service.recusaOS(id);
        return ResponseEntity.ok().build();
    }
}

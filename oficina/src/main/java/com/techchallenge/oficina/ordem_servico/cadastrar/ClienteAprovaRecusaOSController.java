package com.techchallenge.oficina.ordem_servico.cadastrar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

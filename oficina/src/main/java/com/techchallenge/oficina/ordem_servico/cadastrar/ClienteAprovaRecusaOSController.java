package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Cliente aprova ou recusa Ordem de Serviço", description = "Cliente aprova ou recusa ordem de serviço")
@RestController
@RequestMapping({"/public/os/{id}", "/ordens-servico/public/os/{id}"})
public class ClienteAprovaRecusaOSController {

    private final ClienteAprovaRecusaOSService service;

    public ClienteAprovaRecusaOSController(ClienteAprovaRecusaOSService service) {
        this.service = service;
    }

    @PostMapping("/recusar")
    @Operation(summary = "Recusa ordem de serviço", description = "Recusa ordem de serviço")
    public ResponseEntity recusar(@PathVariable("id") UUID id) {

        service.recusaOS(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/aprovar")
    @Operation(summary = "Aprova ordem de serviço", description = "Aprova ordem de serviço")
    public ResponseEntity incluiPecasInsumosOS(@PathVariable("id") UUID id) {

        service.aprovaOS(id);
        return ResponseEntity.ok().build();
    }
}

package com.techchallenge.oficina.pecasinsumos.cadastrar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Criar Peças e Insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class CadastrarPecaInsumoController {

    private final CadastrarPecaInsumoService pecaInsumoService;

    public CadastrarPecaInsumoController(CadastrarPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @PostMapping
    public ResponseEntity<PecaInsumoResponse> cadastrar(@Valid @RequestBody CadastrarPecaInsumoRequest request) {
        PecaInsumoResponse pecaInsumoResponse = pecaInsumoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(pecaInsumoResponse.id());
        return ResponseEntity.created(location).body(pecaInsumoResponse);
    }
}

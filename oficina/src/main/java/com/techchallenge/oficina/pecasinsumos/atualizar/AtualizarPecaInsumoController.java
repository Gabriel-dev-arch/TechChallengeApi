package com.techchallenge.oficina.pecasinsumos.atualizar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Atualizar Peças e Insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class AtualizarPecaInsumoController {

    private final AtualizarPecaInsumoService pecaInsumoService;

    public AtualizarPecaInsumoController(AtualizarPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @PutMapping("/{id}")
    public PecaInsumoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarPecaInsumoRequest request){
        return pecaInsumoService.atualizar(id, request);
    }
}

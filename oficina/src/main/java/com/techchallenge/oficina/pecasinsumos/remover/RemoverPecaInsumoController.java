package com.techchallenge.oficina.pecasinsumos.remover;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Deletar Peças e Insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class RemoverPecaInsumoController {

    private final RemoverPecaInsumoService removerPecaInsumoService;

    public RemoverPecaInsumoController(RemoverPecaInsumoService removerPecaInsumoService) {
        this.removerPecaInsumoService = removerPecaInsumoService;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        removerPecaInsumoService.remover(id);
    }
}

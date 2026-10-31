package com.techchallenge.oficina.pecasinsumos.remover;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Peças e Insumos", description = "Cadastro, consulta e controle de estoque de peças e insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class RemoverPecaInsumoController {

    private final RemoverPecaInsumoService removerPecaInsumoService;

    public RemoverPecaInsumoController(RemoverPecaInsumoService removerPecaInsumoService) {
        this.removerPecaInsumoService = removerPecaInsumoService;
    }

    @Operation(summary = "Remover peça ou insumo", description = "Remoção lógica: o item é desativado e deixa de aparecer na listagem. Não é permitida enquanto houver quantidade reservada.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        removerPecaInsumoService.remover(id);
    }
}

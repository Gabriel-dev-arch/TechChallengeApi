package com.techchallenge.oficina.pecasinsumos.atualizar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Peças e Insumos", description = "Cadastro, consulta e controle de estoque de peças e insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class AtualizarPecaInsumoController {

    private final AtualizarPecaInsumoService pecaInsumoService;

    public AtualizarPecaInsumoController(AtualizarPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @Operation(summary = "Atualizar peça ou insumo", description = "Atualiza código, nome, descrição e preço. Tipo, unidade de medida e quantidades não são alterados por esta operação.")
    @PutMapping("/{id}")
    public PecaInsumoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarPecaInsumoRequest request){
        return pecaInsumoService.atualizar(id, request);
    }
}

package com.techchallenge.oficina.pecasinsumos.baixar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Peças e Insumos", description = "Cadastro, consulta e controle de estoque de peças e insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class BaixarPecaInsumoController {

    private final BaixarPecaInsumoService pecaInsumoService;

    public BaixarPecaInsumoController(BaixarPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @Operation(summary = "Registrar saída de estoque", description = "Baixa manual de estoque (perda, quebra, ajuste de inventário). Não pode usar a quantidade reservada para ordens de serviço.")
    @PostMapping("/{id}/saidas")
    public PecaInsumoResponse baixar(@PathVariable UUID id, @Valid
    @RequestBody BaixarPecaInsumoRequest request){
        return pecaInsumoService.baixar(id, request);
    }
}

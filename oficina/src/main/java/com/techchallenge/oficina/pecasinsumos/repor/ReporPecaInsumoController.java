package com.techchallenge.oficina.pecasinsumos.repor;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Peças e Insumos", description = "Cadastro, consulta e controle de estoque de peças e insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class ReporPecaInsumoController {

    private final ReporPecaInsumoService pecaInsumoService;

    public ReporPecaInsumoController(ReporPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @Operation(summary = "Registrar entrada de estoque", description = "Soma a quantidade informada ao estoque total do item.")
    @PostMapping("/{id}/entradas")
    public PecaInsumoResponse repor(@PathVariable UUID id, @Valid
                                    @RequestBody ReporPecaInsumoRequest request){
        return pecaInsumoService.repor(id, request);
    }
}

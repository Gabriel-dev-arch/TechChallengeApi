package com.techchallenge.oficina.pecasinsumos.baixar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Baixar Peças e Insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class BaixarPecaInsumoController {

    private final BaixarPecaInsumoService pecaInsumoService;

    public BaixarPecaInsumoController(BaixarPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @PostMapping("/{id}/saidas")
    public PecaInsumoResponse baixar(@PathVariable UUID id, @Valid
    @RequestBody BaixarPecaInsumoRequest request){
        return pecaInsumoService.baixar(id, request);
    }
}

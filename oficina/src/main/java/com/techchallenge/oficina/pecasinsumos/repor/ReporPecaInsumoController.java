package com.techchallenge.oficina.pecasinsumos.repor;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Repor Peças e Insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class ReporPecaInsumoController {

    private final ReporPecaInsumoService pecaInsumoService;

    public ReporPecaInsumoController(ReporPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @PostMapping("/{id}/entradas")
    public PecaInsumoResponse repor(@PathVariable UUID id, @Valid
                                    @RequestBody ReporPecaInsumoRequest request){
        return pecaInsumoService.repor(id, request);
    }
}

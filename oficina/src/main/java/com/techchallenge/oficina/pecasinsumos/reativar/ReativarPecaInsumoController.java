package com.techchallenge.oficina.pecasinsumos.reativar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Reativar Peças e Insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class ReativarPecaInsumoController {

    private final ReativarPecaInsumoService reativarPecaInsumoService;

    public ReativarPecaInsumoController(ReativarPecaInsumoService reativarPecaInsumoService) {
        this.reativarPecaInsumoService = reativarPecaInsumoService;
    }

    @PostMapping("/{id}/reativacao")
    public PecaInsumoResponse reativar(@PathVariable UUID id) {
        return reativarPecaInsumoService.reativar(id);
    }
}

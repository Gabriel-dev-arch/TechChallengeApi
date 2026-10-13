package com.techchallenge.oficina.pecasinsumos.reativar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Peças e Insumos", description = "Cadastro, consulta e controle de estoque de peças e insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class ReativarPecaInsumoController {

    private final ReativarPecaInsumoService reativarPecaInsumoService;

    public ReativarPecaInsumoController(ReativarPecaInsumoService reativarPecaInsumoService) {
        this.reativarPecaInsumoService = reativarPecaInsumoService;
    }

    @Operation(summary = "Reativar peça ou insumo", description = "Reativa um item removido, que volta a aparecer na listagem e a aceitar movimentações de estoque.")
    @PostMapping("/{id}/reativacao")
    public PecaInsumoResponse reativar(@PathVariable UUID id) {
        return reativarPecaInsumoService.reativar(id);
    }
}

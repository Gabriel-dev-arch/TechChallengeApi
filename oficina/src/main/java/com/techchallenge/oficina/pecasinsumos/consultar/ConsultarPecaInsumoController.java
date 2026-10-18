package com.techchallenge.oficina.pecasinsumos.consultar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Peças e Insumos", description = "Cadastro, consulta e controle de estoque de peças e insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class ConsultarPecaInsumoController {

    private final ConsultarPecaInsumoService pecaInsumoService;

    public ConsultarPecaInsumoController(ConsultarPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @Operation(summary = "Listar peças e insumos", description = "Lista os itens ativos, paginados e ordenados por nome, com filtros opcionais por tipo e nome.")
    @GetMapping
    public PagedModel<PecaInsumoResponse> listar(@RequestParam(required = false) TipoItem tipo,
                                                 @RequestParam(required = false) String nome,
                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome"));
        return new PagedModel<>(pecaInsumoService.listar(tipo, nome, pageable));
    }

    @Operation(summary = "Buscar peça ou insumo pelo ID", description = "Retorna o item com os saldos de estoque, inclusive se estiver inativo.")
    @GetMapping("/{id}")
    public PecaInsumoResponse buscar(@PathVariable UUID id){
        return pecaInsumoService.buscar(id);
    }
}

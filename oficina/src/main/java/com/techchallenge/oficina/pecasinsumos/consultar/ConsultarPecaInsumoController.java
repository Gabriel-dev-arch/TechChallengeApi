package com.techchallenge.oficina.pecasinsumos.consultar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Consultar Peças e Insumos")
@RestController
@RequestMapping("/pecas-insumos")
public class ConsultarPecaInsumoController {

    private final ConsultarPecaInsumoService pecaInsumoService;

    public ConsultarPecaInsumoController(ConsultarPecaInsumoService pecaInsumoService) {
        this.pecaInsumoService = pecaInsumoService;
    }

    @GetMapping
    public PagedModel<PecaInsumoResponse> listar(@RequestParam(required = false) TipoItem tipo,
                                                 @RequestParam(required = false) String nome,
                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome"));
        return new PagedModel<>(pecaInsumoService.listar(tipo, nome, pageable));
    }

    @GetMapping("/{id}")
    public PecaInsumoResponse buscar(@PathVariable UUID id){
        return pecaInsumoService.buscar(id);
    }
}

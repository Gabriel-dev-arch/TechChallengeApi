package com.techchallenge.oficina.pecasinsumos.cadastrar;

import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CadastrarPecaInsumoRequest(
        @NotNull TipoItem tipo,
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @NotNull UnidadeMedida unidadeMedida,
        @NotNull @PositiveOrZero BigDecimal precoUnitario,
        @PositiveOrZero BigDecimal quantidadeInicial
) {
}
package com.techchallenge.oficina.pecasinsumos.cadastrar;

import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Os limites de @Digits seguem as colunas numeric(12,2) e numeric(12,3); sem eles o banco arredondaria em silêncio.
public record CadastrarPecaInsumoRequest(
        @NotNull TipoItem tipo,
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @NotNull UnidadeMedida unidadeMedida,
        @NotNull @PositiveOrZero
        @Digits(integer = 10, fraction = 2, message = "deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais")
        BigDecimal precoUnitario,
        @PositiveOrZero
        @Digits(integer = 9, fraction = 3, message = "deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais")
        BigDecimal quantidadeInicial
) {
}
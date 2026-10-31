package com.techchallenge.oficina.pecasinsumos.atualizar;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AtualizarPecaInsumoRequest(
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @NotNull @PositiveOrZero
        @Digits(integer = 10, fraction = 2, message = "deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais")
        BigDecimal precoUnitario
) {
}

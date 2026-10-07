package com.techchallenge.oficina.pecasinsumos.baixar;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BaixarPecaInsumoRequest(
        @NotNull @Positive
        @Digits(integer = 9, fraction = 3, message = "deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais")
        BigDecimal quantidade
        ) {
}

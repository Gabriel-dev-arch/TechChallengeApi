package com.techchallenge.oficina.pecasinsumos.baixar;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BaixarPecaInsumoRequest(
        @NotNull @Positive BigDecimal quantidade
        ) {
}

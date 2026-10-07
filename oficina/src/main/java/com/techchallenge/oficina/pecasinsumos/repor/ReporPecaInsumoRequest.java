package com.techchallenge.oficina.pecasinsumos.repor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReporPecaInsumoRequest(
        @NotNull @Positive BigDecimal quantidade
) {
}

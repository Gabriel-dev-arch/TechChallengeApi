package com.techchallenge.oficina.ordem_servico.cadastrar.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicosRequest(
        @NotNull UUID id,
        String descricao,
        BigDecimal valor ){
}

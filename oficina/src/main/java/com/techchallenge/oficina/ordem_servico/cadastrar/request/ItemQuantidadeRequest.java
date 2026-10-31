package com.techchallenge.oficina.ordem_servico.cadastrar.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/** A quantidade é validada pela OS e pelo estoque (maior que zero, unidade e saldo disponível). */
public record ItemQuantidadeRequest(
        @NotNull UUID id,
        BigDecimal quantidade){
}

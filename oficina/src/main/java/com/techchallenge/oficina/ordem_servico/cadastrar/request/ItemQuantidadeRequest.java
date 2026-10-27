package com.techchallenge.oficina.ordem_servico.cadastrar.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemQuantidadeRequest(
        UUID id,
        BigDecimal quantidade){
}

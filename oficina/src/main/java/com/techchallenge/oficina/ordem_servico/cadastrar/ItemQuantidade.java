package com.techchallenge.oficina.ordem_servico.cadastrar;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemQuantidade(
        UUID itens,
        BigDecimal quantidade){
}

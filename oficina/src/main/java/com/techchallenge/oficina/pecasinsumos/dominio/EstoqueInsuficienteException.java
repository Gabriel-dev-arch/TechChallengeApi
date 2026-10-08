package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

import java.math.BigDecimal;
import java.util.UUID;

public class EstoqueInsuficienteException extends ConflitoException {

    public EstoqueInsuficienteException(UUID id, BigDecimal quantidadeSolicitada, BigDecimal quantidadeDisponivel) {
        super("Estoque insuficiente para a peça/insumo %s: solicitado %s, disponível %s"
                .formatted(id, quantidadeSolicitada.stripTrailingZeros().toPlainString(),
                        quantidadeDisponivel.stripTrailingZeros().toPlainString()));
    }
}

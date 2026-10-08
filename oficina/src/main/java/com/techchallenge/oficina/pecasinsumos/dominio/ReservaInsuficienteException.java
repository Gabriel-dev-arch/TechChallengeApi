package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

import java.math.BigDecimal;
import java.util.UUID;

public class ReservaInsuficienteException extends ConflitoException {
    public ReservaInsuficienteException(UUID id, BigDecimal solicitado, BigDecimal reservado) {
        super("Reserva insuficiente para a peça/insumo %s: solicitado %s, reservado %s"
                .formatted(id, solicitado.stripTrailingZeros().toPlainString(),
                        reservado.stripTrailingZeros().toPlainString()));
    }
}

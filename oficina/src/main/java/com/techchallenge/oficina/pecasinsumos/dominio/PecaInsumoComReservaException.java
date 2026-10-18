package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

import java.util.UUID;

public class PecaInsumoComReservaException extends ConflitoException {
    public PecaInsumoComReservaException(UUID id) {
        super("A peça/insumo possui quantidade reservada e não é possível desativar no momento: " + id);
    }
}

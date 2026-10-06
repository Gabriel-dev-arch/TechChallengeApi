package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

import java.util.UUID;

public class PecaInsumoInativoException extends ConflitoException {
    public PecaInsumoInativoException(UUID id) {
        super("Peça/Insumo está inativo! " + id);
    }
}

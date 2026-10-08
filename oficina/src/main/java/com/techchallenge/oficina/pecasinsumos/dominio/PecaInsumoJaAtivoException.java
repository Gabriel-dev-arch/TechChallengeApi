package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

import java.util.UUID;

public class PecaInsumoJaAtivoException extends ConflitoException {
    public PecaInsumoJaAtivoException(UUID id) {
        super("Peça/Insumo já está ativo: " + id);
    }
}

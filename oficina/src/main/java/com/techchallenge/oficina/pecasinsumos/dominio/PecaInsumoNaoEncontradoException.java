package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class PecaInsumoNaoEncontradoException extends RecursoNaoEncontradoException {

    public PecaInsumoNaoEncontradoException(UUID id) {
        super("Peça/Insumo não encontrado: " + id);
    }
}

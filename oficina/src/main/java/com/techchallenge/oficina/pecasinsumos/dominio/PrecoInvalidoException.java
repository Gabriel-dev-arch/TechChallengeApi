package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.RegraInvalidaException;

public class PrecoInvalidoException extends RegraInvalidaException {
    public PrecoInvalidoException(String message) {
        super("Preço Invalido: " + message);
    }
}

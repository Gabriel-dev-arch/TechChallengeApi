package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.RegraInvalidaException;

public class QuantidadeInvalidaException extends RegraInvalidaException {
    public QuantidadeInvalidaException(String message) {
        super("Quantidade Inválida: " + message);
    }
}

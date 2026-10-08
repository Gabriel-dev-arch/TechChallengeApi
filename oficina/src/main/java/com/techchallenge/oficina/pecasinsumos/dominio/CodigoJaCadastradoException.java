package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

public class CodigoJaCadastradoException extends ConflitoException {

    public CodigoJaCadastradoException(String codigo) {
        super("Já existe uma peça/insumo ativa com o código " + codigo);
    }
}

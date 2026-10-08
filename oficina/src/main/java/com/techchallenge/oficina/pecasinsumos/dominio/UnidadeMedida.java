package com.techchallenge.oficina.pecasinsumos.dominio;

import java.math.BigDecimal;

public enum UnidadeMedida {
    UN("Unidade", false),
    JOGO("Jogo", false),
    L("Litro", true),
    ML("Mililitro", true),
    KG("Quilograma", true),
    G("Grama", true);

    private final String descricao;
    private final boolean fracionavel;

    UnidadeMedida(String descricao, boolean fracionavel) {
        this.descricao = descricao;
        this.fracionavel = fracionavel;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFracionavel() {
        return fracionavel;
    }

    public boolean aceita(BigDecimal quantidade){
        return fracionavel || quantidade.stripTrailingZeros().scale() <= 0;
    }

}

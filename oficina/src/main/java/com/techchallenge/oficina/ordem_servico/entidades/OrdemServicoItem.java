package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdemServicoItem {

    @ManyToOne(optional = false)
    @JoinColumn(name = "itens_id", nullable = false)
    private PecaInsumo item;

    @Column(name = "quantidade", nullable = false, precision = 38, scale = 2)
    private BigDecimal quantidade;

    public OrdemServicoItem(PecaInsumo item, BigDecimal quantidade) {
        this.item =item;
        this.quantidade = quantidade;
    }

    public void adicionarQuantidade(BigDecimal quantidade) {
        this.quantidade = this.quantidade.add(quantidade);
    }
}

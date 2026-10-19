package com.techchallenge.oficina.ordem_servico.dominio;

import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;

import java.math.BigDecimal;
import java.util.List;

public record Orcamento() {

    public static BigDecimal from(
            List<PecaInsumo> itens,
            List<Servico> servicos) {

        BigDecimal valorServico = servicos.stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorPecaInsumos = itens.stream()
                .map(PecaInsumo::getPrecoUnitario)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return valorServico.add(valorPecaInsumos);
    }


}

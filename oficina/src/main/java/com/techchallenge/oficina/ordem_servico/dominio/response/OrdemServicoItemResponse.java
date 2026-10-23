package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.ordem_servico.entidades.OrdemServicoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;

import java.math.BigDecimal;

public record OrdemServicoItemResponse(
        TipoItem tipo,
        String codigo,
        String nome,
        String descricao,
        UnidadeMedida unidadeMedida,
        BigDecimal precoUnitario,
        BigDecimal quantidade) {

    public static OrdemServicoItemResponse from(OrdemServicoItem itemOrdemServico) {
        var item = itemOrdemServico.getItem();
        return new OrdemServicoItemResponse(
                item.getTipo(),
                item.getCodigo(),
                item.getNome(),
                item.getDescricao(),
                item.getUnidadeMedida(),
                item.getPrecoUnitario(),
                itemOrdemServico.getQuantidade());
    }
}

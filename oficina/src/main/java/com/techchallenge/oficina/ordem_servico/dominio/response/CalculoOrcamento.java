package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.ordem_servico.cadastrar.CadastrarOSRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.ItemQuantidade;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public record CalculoOrcamento(
        List<Servico> servicos,
        List<ItemQuantidade> itens,
        List<PecaInsumo> pecaInsumos) {

    public static BigDecimal realizaCalculoTotal(List<PecaInsumo> pecaInsumos, List<ItemQuantidade> itens, List<Servico> servico){

        var pecasInsumos = realizaCalculoItensInsumosOrcamento(pecaInsumos, itens);
        var valorServicos = realizaCalculoServicos(servico);

        return pecasInsumos.add(valorServicos);
    }

    private static BigDecimal realizaCalculoItensInsumosOrcamento(List<PecaInsumo> pecaInsumos, List<ItemQuantidade> itens){

        Map<UUID, PecaInsumo> pecasPorId = pecaInsumos.stream()
                .collect(Collectors.toMap(PecaInsumo::getId, Function.identity()));

        var s = itens.stream()
                .map(item -> {
                    return pecasPorId.get(item.itens()).getPrecoUnitario().multiply(item.quantidade());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return s;
    }

    private static BigDecimal realizaCalculoServicos(List<Servico> servicos){

        var s = servicos.stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return s;
    }
}

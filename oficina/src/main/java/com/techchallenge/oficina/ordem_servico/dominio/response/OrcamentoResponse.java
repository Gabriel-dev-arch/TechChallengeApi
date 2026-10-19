package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

import java.math.BigDecimal;

public record OrcamentoResponse(
        BigDecimal total,
        String status) {

    public static OrcamentoResponse from(BigDecimal valorOrcamento, StatusOS status) {

        return new OrcamentoResponse(valorOrcamento, status.getDescricao());
    }
}
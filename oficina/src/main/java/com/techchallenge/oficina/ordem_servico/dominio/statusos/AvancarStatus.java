package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;

public interface AvancarStatus {

    public OrdemServico avancar(OrdemServico osCadastrada);
}

package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

import java.math.BigDecimal;
import java.util.Collections;

public class AguardandoAprovacao implements AvancarStatus{

    private final OrdemServicoRepository repository;

    public AguardandoAprovacao(OrdemServico os, OrdemServicoRepository repository) {
        this.repository = repository;
        avancar(os);
    }

    public OrdemServico avancar(OrdemServico osCadastrada) {


        if(!StatusOS.EM_DIAGNOSTICO.equals(osCadastrada.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status EM_DIAGNOSTICO.");
        }
        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.AGUARDANDO_APROVACAO);

        return repository.saveAndFlush(ordemServico);
    }
}

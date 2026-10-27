package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class AprovarOuNegar implements AvancarStatus {

    private final OrdemServicoRepository repository;

    public AprovarOuNegar(OrdemServico os, OrdemServicoRepository repository) {
        this.repository = repository;
        avancar(os);
    }

    public OrdemServico avancar(OrdemServico osCadastrada) {

        OrdemServico ordemServico = null;

        if(StatusOS.APROVADA.equals(osCadastrada.getStatus())){
            ordemServico = osCadastrada.atualizaStatus(StatusOS.EM_EXECUCAO);
        }
        if (StatusOS.NEGADA.equals(osCadastrada.getStatus())) {
            ordemServico = osCadastrada.atualizaStatus(StatusOS.FINALIZADA);
        }

        return repository.saveAndFlush(ordemServico);
    }
}
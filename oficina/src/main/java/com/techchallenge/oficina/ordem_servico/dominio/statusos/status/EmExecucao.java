package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class EmExecucao implements AvancarStatus {

    private final OrdemServicoRepository repository;


    public EmExecucao(OrdemServico os, OrdemServicoRepository repository) {
        this.repository = repository;
        avancar(os);
    }

    @Override
    public OrdemServico avancar(OrdemServico osCadastrada) {

        if(!StatusOS.EM_EXECUCAO.equals(osCadastrada.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status RECEBIDA.");
        }
        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.FINALIZADA);
        return repository.saveAndFlush(ordemServico);
    }
}

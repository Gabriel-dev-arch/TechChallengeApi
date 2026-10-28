package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
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
            throw new TransicaoInvalidaException("Status inválido. A ordem de serviço deve estar com status EM_EXECUCAO.");
        }
        // serviço executado: as peças reservadas saem do estoque
        osCadastrada.consumirReservas();
        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.FINALIZADA);
        return repository.saveAndFlush(ordemServico);
    }
}

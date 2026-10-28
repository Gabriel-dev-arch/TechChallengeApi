package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class Aprovada implements AvancarStatus {

    private final OrdemServicoRepository repository;


    public Aprovada(OrdemServico os, OrdemServicoRepository repository) {
        this.repository = repository;
        avancar(os);
    }

    @Override
    public OrdemServico avancar(OrdemServico osCadastrada) {

        if(!StatusOS.APROVADA.equals(osCadastrada.getStatus())){
            throw new TransicaoInvalidaException("Status inválido. A ordem de serviço deve estar com status APROVADA.");
        }

        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.EM_EXECUCAO);

        return repository.saveAndFlush(ordemServico);
    }
}

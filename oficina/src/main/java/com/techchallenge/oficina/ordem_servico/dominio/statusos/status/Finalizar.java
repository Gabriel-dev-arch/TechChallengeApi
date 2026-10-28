package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class Finalizar implements AvancarStatus {

    private final OrdemServicoRepository repository;

    public Finalizar(OrdemServico os, OrdemServicoRepository repository) {
        this.repository = repository;
        avancar(os);
    }

    @Override
    public OrdemServico avancar(OrdemServico os) {

        if(!StatusOS.FINALIZADA.equals(os.getStatus())){
            throw new TransicaoInvalidaException("Status inválido. A ordem de serviço deve estar com status FINALIZADA.");
        }
        OrdemServico ordemServico = os.atualizaStatus(StatusOS.ENTREGUE);
        return repository.saveAndFlush(ordemServico);
    }
}

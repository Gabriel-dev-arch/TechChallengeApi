package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class EmDiagnostico implements AvancarStatus{

    private final OrdemServicoRepository repository;

    public EmDiagnostico(OrdemServico os, OrdemServicoRepository repository) {
        this.repository = repository;
        avancar(os);
    }

    public OrdemServico avancar(OrdemServico os) {

        if(!StatusOS.RECEBIDA.equals(os.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status RECEBIDA.");
        }
        OrdemServico ordemServico = os.atualizaStatus(StatusOS.EM_DIAGNOSTICO);
        return repository.saveAndFlush(ordemServico);
    }
}

package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;


public class OsRecebida implements AvancarStatus {

    private final OrdemServicoRepository repository;

    public OsRecebida(OrdemServico osCadastrada, OrdemServicoRepository repository) {
        this.repository = repository;
        avancar(osCadastrada);
    }

    @Override
    public OrdemServico avancar(OrdemServico osCadastrada) {

        if(!StatusOS.EM_DIAGNOSTICO.equals(osCadastrada.getStatus())){
            throw new TransicaoInvalidaException("Status inválido. A ordem de serviço deve estar com status EM_DIAGNOSTICO.");
        }

        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.AGUARDANDO_APROVACAO);

        return repository.saveAndFlush(ordemServico);

    }
}

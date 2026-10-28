package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class AprovarOuNegar extends AvancarStatusOSAbstract {

    public AprovarOuNegar(OrdemServico os, OrdemServicoRepository repository) {
        super(os, repository);
    }

    public OrdemServico avancar(OrdemServico osCadastrada, OrdemServicoRepository repository) {

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
package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class ExecutarServico extends AvancarStatusOSAbstract {

    public ExecutarServico(OrdemServico os, OrdemServicoRepository repository) {
        super(os, repository);
    }

    @Override
    public OrdemServico avancar(OrdemServico osCadastrada,  OrdemServicoRepository repository) {

        if(!StatusOS.EM_EXECUCAO.equals(osCadastrada.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status RECEBIDA.");
        }
        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.FINALIZADA);
        return repository.saveAndFlush(ordemServico);
    }
}

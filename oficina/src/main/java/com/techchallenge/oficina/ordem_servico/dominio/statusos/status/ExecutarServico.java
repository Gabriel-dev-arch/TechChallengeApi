package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.context.ApplicationEventPublisher;

public class ExecutarServico extends AvancarStatusOSAbstract {

    public ExecutarServico(OrdemServico os, OrdemServicoRepository repository,
                           ApplicationEventPublisher eventPublisher) {

        super(os, repository, eventPublisher);
    }

    @Override
    public void validarStatus(OrdemServico os) {
        if(!StatusOS.EM_EXECUCAO.equals(os.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status RECEBIDA.");
        }
    }

    @Override
    public OrdemServico avancar(OrdemServico os, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher) {
        OrdemServico ordemServico = os.atualizaStatus(StatusOS.FINALIZADA);
        eventPublisher.publishEvent(OSCriadaEvent.builder().ordemServico(os).build());
        return repository.saveAndFlush(ordemServico);
    }
}

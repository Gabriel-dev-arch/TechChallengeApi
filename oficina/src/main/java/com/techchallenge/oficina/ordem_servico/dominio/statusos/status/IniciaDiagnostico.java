package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.context.ApplicationEventPublisher;

public class IniciaDiagnostico extends AvancarStatusOSAbstract {

    public IniciaDiagnostico(OrdemServico os,
                             OrdemServicoRepository repository,
                             ApplicationEventPublisher eventPublisher) {

        super(os, repository, eventPublisher);
    }

    public OrdemServico avancar(OrdemServico os, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher) {
        OrdemServico ordemServico = os.atualizaStatus(StatusOS.EM_DIAGNOSTICO);
        eventPublisher.publishEvent(OSCriadaEvent.builder().ordemServico(os).build());
        return repository.save(ordemServico);
    }

    public void validarStatus(OrdemServico osCadastrada) {
        if(!StatusOS.RECEBIDA.equals(osCadastrada.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status Recebida.");
        }
    }

}

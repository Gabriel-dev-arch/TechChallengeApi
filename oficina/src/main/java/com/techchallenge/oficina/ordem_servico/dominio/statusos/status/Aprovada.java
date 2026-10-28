package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.context.ApplicationEventPublisher;

public class Aprovada extends AvancarStatusOSAbstract {

    public Aprovada(OrdemServico os, OrdemServicoRepository repository,
                    ApplicationEventPublisher eventPublisher) {

        super(os, repository, eventPublisher);
    }

    @Override
    public void validarStatus(OrdemServico os) {
        if(!StatusOS.APROVADA.equals(os.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status aguardando aprovação.");
        }
    }

    @Override
    public OrdemServico avancar(OrdemServico osCadastrada, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher) {

        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.EM_EXECUCAO);
        eventPublisher.publishEvent(OSCriadaEvent.builder().ordemServico(osCadastrada).build());
        return repository.saveAndFlush(ordemServico);
    }
}

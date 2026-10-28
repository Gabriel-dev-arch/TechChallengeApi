package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.context.ApplicationEventPublisher;

public class OsRecebida extends AvancarStatusOSAbstract {


    public OsRecebida(OrdemServico os, OrdemServicoRepository repository,
                      ApplicationEventPublisher eventPublisher) {

        super(os, repository, eventPublisher);

    }

    @Override
    public void validarStatus(OrdemServico os) {
        if(!StatusOS.EM_DIAGNOSTICO.equals(os.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status EM_DIAGNOSTICO.");
        }
    }


    @Override
    public OrdemServico avancar(OrdemServico osCadastrada, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher) {

        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.AGUARDANDO_APROVACAO);
        eventPublisher.publishEvent(OSCriadaEvent.builder().ordemServico(osCadastrada).build());
        return repository.saveAndFlush(ordemServico);
    }

}

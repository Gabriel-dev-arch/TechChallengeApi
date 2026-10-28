package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import org.springframework.context.ApplicationEventPublisher;

public abstract class AvancarStatusOSAbstract {

    protected AvancarStatusOSAbstract(OrdemServico os, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher){
        validarStatus(os);
        avancar(os, repository, eventPublisher);
    }

    public abstract void validarStatus(OrdemServico os);
    public abstract OrdemServico avancar(OrdemServico os, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher);
}

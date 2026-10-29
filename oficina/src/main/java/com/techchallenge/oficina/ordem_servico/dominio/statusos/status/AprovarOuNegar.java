package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.context.ApplicationEventPublisher;

public class AprovarOuNegar extends AvancarStatusOSAbstract {

    public AprovarOuNegar(OrdemServico os,
                    OrdemServicoRepository repository,
                    ApplicationEventPublisher eventPublisher) {

        super(os, repository, eventPublisher);
    }

    @Override
    public void validarStatus(OrdemServico osCadastrada) {
        if(!StatusOS.AGUARDANDO_APROVACAO.equals(osCadastrada.getStatus())){
            throw new RuntimeException(" A ordem de serviço deve estar com status aguardando aprovação.");
        }
    }

    public OrdemServico avancar(OrdemServico osCadastrada, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher) {

        OrdemServico ordemServico = null;

        if(StatusOS.APROVADA.equals(osCadastrada.getStatus())){
            ordemServico = osCadastrada.atualizaStatus(StatusOS.EM_EXECUCAO);
        }
        if (StatusOS.NEGADA.equals(osCadastrada.getStatus())) {
            ordemServico = osCadastrada.atualizaStatus(StatusOS.FINALIZADA);
        }
        osCadastrada.incluirDiagnostico();
        eventPublisher.publishEvent(OSCriadaEvent.builder().ordemServico(osCadastrada).build());
        return repository.saveAndFlush(ordemServico);
    }

}
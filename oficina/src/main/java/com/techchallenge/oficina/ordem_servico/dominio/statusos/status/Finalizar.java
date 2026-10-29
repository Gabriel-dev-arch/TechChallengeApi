package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.context.ApplicationEventPublisher;

public class Finalizar extends AvancarStatusOSAbstract {


    public Finalizar(OrdemServico os,
                           OrdemServicoRepository repository,
                           ApplicationEventPublisher eventPublisher) {

        super(os, repository, eventPublisher);

    }

    @Override
    public void validarStatus(OrdemServico osCadastrada) {
        if(!StatusOS.FINALIZADA.equals(osCadastrada.getStatus()) &&
                !StatusOS.EM_EXECUCAO.equals(osCadastrada.getStatus())){
            throw new TransicaoInvalidaException("Status inválido. A ordem de serviço deve estar com status Em execução.");
        }
    }

    @Override
    public OrdemServico avancar(OrdemServico os, OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher) {

        OrdemServico ordemServico = os.atualizaStatus(StatusOS.ENTREGUE);
        eventPublisher.publishEvent(OSCriadaEvent.builder().ordemServico(os).build());
        return repository.saveAndFlush(ordemServico);
    }

}

package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.status.*;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EstadosOS {

    private final OrdemServicoRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public EstadosOS(OrdemServicoRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public AvancarStatusOSAbstract executa(OrdemServico osCadastrada, StatusOS step) {

        return switch (step) {
            case RECEBIDA       -> new IniciaDiagnostico    (osCadastrada, repository, eventPublisher);
            case EM_DIAGNOSTICO -> new AguardandoAprovacao  (osCadastrada, repository, eventPublisher);
            case APROVADA       -> new AprovarOuNegar       (osCadastrada, repository, eventPublisher);
            case NEGADA         -> new AprovarOuNegar       (osCadastrada, repository, eventPublisher);
            case EM_EXECUCAO    -> new ExecutarServico      (osCadastrada, repository, eventPublisher);
            case FINALIZADA     -> new Finalizar            (osCadastrada, repository, eventPublisher);
            case ENTREGUE       -> new EntregarVeiculo      (osCadastrada, repository, eventPublisher);
            default -> throw new IllegalStateException("Unexpected value: " + osCadastrada.getStatus());
        };
    }
}

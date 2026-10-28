package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.status.*;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import org.springframework.stereotype.Component;

@Component
public class EstadosOS {

    private final OrdemServicoRepository repository;

    private EstadosOS(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    public AvancarStatusOSAbstract executa(OrdemServico osCadastrada) {

        return switch (osCadastrada.getStatus()) {
            case RECEBIDA       -> new IniciaDiagnostico(osCadastrada, repository);
            case EM_DIAGNOSTICO -> new AguardandoAprovacao(osCadastrada, repository);
            case APROVADA       -> new AprovarOuNegar(osCadastrada, repository);
            case NEGADA         -> new AprovarOuNegar(osCadastrada, repository);
            case EM_EXECUCAO    -> new ExecutarServico(osCadastrada, repository);
            case FINALIZADA     -> new Finalizar(osCadastrada, repository);
            default -> throw new IllegalStateException("Unexpected value: " + osCadastrada.getStatus());
        };
    }
}

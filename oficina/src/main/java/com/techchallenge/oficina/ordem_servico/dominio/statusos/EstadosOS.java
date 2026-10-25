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

    public AvancarStatus executa(OrdemServico osCadastrada) {

        return switch (osCadastrada.getStatus()) {
            case RECEBIDA -> new EmDiagnostico(osCadastrada, repository);
            case EM_DIAGNOSTICO -> new AguardandoAprovacao(osCadastrada, repository);
            case AGUARDANDO_APROVACAO -> new AprovarOuNegar(osCadastrada, repository);
            case EM_EXECUCAO -> new EmExecucao(osCadastrada, repository);
            case ENTREGUE -> new Finalizar(osCadastrada, repository);
            default -> throw new IllegalStateException("Unexpected value: " + osCadastrada.getStatus());
        };
    }
}

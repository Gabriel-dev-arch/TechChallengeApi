package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.techchallenge.oficina.ordem_servico.entidades.StatusOS.*;

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
            case AGUARDANDO_APROVACAO -> new AguardandoAprovacao(osCadastrada, repository);
            case EM_EXECUCAO -> null;
            case FINALIZADA -> null;
            case ENTREGUE -> null;
        };
    }
}

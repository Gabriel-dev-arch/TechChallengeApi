package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClienteAprovaRecusaOSService {

    private final OrdemServicoRepository repository;

    public ClienteAprovaRecusaOSService(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    public void aprovaOS(UUID id) {

        OrdemServico os = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id))
                .atualizaStatus(StatusOS.APROVADA);
        repository.saveAndFlush(os);
    }

    public void recusaOS(UUID id) {

        OrdemServico os = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id))
                .atualizaStatus(StatusOS.NEGADA);
        repository.saveAndFlush(os);
    }
}

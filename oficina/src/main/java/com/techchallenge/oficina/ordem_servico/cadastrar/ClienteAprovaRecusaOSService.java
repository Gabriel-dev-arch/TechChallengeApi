package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.cadastrar.request.CadastrarOSRequest;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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

package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Rotas do cliente: o CLIENTE só aprova ou recusa OS do próprio documento. */
@Service
public class ClienteAprovaRecusaOSService {

    private final OrdemServicoRepository repository;
    private final ControleDeAcesso controleDeAcesso;

    public ClienteAprovaRecusaOSService(OrdemServicoRepository repository, ControleDeAcesso controleDeAcesso) {
        this.repository = repository;
        this.controleDeAcesso = controleDeAcesso;
    }

    public void aprovaOS(UUID id) {
        atualizaStatus(id, StatusOS.APROVADA);
    }

    public void recusaOS(UUID id) {
        atualizaStatus(id, StatusOS.NEGADA);
    }

    private void atualizaStatus(UUID id, StatusOS status) {
        OrdemServico os = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
        controleDeAcesso.verificarDono(os.getCliente().getDocumento());
        repository.saveAndFlush(os.atualizaStatus(status));
    }
}

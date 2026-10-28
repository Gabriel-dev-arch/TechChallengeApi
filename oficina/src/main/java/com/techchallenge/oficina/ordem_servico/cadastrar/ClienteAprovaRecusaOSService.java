package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void aprovaOS(UUID id) {
        OrdemServico os = buscarOSDoCliente(id);
        repository.saveAndFlush(os.atualizaStatus(StatusOS.APROVADA));
    }

    /** Recusa o orçamento e devolve ao estoque as peças reservadas para a OS. */
    @Transactional
    public void recusaOS(UUID id) {
        OrdemServico os = buscarOSDoCliente(id);
        os.liberarReservas();
        repository.saveAndFlush(os.atualizaStatus(StatusOS.NEGADA));
    }

    private OrdemServico buscarOSDoCliente(UUID id) {
        OrdemServico os = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
        controleDeAcesso.verificarDono(os.getCliente().getDocumento());
        return os;
    }
}

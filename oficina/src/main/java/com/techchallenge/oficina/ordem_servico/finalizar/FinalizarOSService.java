package com.techchallenge.oficina.ordem_servico.finalizar;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.EstadosOS;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.UUID;

@Service
public class FinalizarOSService {

    private final OrdemServicoRepository repository;
    private final EntityManager entityManager;
    private final EstadosOS estadosOS;

    public FinalizarOSService(OrdemServicoRepository repository, EntityManager entityManager, EstadosOS estadosOS) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.estadosOS = estadosOS;
    }

    @Transactional
    public OrdemServicoResponse finalizar(UUID id) {
        OrdemServico os = repository.buscarParaFinalizar(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
        if (os.getStatus() == StatusOS.FINALIZADA || os.getStatus() == StatusOS.ENTREGUE) {
            return OrdemServicoResponse.from(os);
        }
        if (os.getStatus() != StatusOS.EM_EXECUCAO) {
            throw new TransicaoInvalidaException("A ordem de serviço deve estar em execução para ser finalizada");
        }
        os.getItens().stream().sorted(Comparator.comparing(PecaInsumo::getId))
                .forEach(item -> entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE));
        os.getItensQuantidade().forEach(item -> item.getItem().validarConsumo(item.getQuantidade()));
        os.consumirReservas();
        estadosOS.executa(os);
        return OrdemServicoResponse.from(os);
    }
}

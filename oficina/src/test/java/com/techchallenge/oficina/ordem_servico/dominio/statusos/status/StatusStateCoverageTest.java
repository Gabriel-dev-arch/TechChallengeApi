package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StatusStateCoverageTest {

    private OrdemServicoRepository repository;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        repository = mock(OrdemServicoRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void executarServicoFinalizaOS() {
        OrdemServico os = criarOS(StatusOS.EM_EXECUCAO);

        new ExecutarServico(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.FINALIZADA, true);
    }

    @Test
    void executarServicoRejeitaStatusInvalido() {
        assertThatThrownBy(() -> new ExecutarServico(criarOS(StatusOS.RECEBIDA), repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    void finalizarEntregaOS() {
        OrdemServico os = criarOS(StatusOS.FINALIZADA);

        new Finalizar(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.ENTREGUE, true);
    }

    @Test
    void finalizarRejeitaStatusInvalido() {
        assertThatThrownBy(() -> new Finalizar(criarOS(StatusOS.RECEBIDA), repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    void aguardandoAprovacaoAtualizaOS() {
        OrdemServico os = criarOS(StatusOS.EM_DIAGNOSTICO);

        new AguardandoAprovacao(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.AGUARDANDO_APROVACAO, true);
    }

    @Test
    void aguardandoAprovacaoRejeitaStatusInvalido() {
        assertThatThrownBy(() -> new AguardandoAprovacao(criarOS(StatusOS.RECEBIDA), repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    void aprovarOrdemAtualizaParaEmExecucao() {
        OrdemServico os = criarOS(StatusOS.APROVADA);

        new AprovarOuNegar(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.EM_EXECUCAO, true);
    }

    @Test
    void negarOrdemAtualizaParaFinalizada() {
        OrdemServico os = criarOS(StatusOS.NEGADA);

        new AprovarOuNegar(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.FINALIZADA, true);
    }

    @Test
    void aprovarOuNegarRejeitaStatusInvalido() {
        assertThatThrownBy(() -> new AprovarOuNegar(criarOS(StatusOS.RECEBIDA), repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("aguardando aprovação");
    }

    @Test
    void aprovadaAtualizaParaEmExecucao() {
        OrdemServico os = criarOS(StatusOS.APROVADA);

        new Aprovada(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.EM_EXECUCAO, true);
    }

    @Test
    void aprovadaRejeitaStatusInvalido() {
        assertThatThrownBy(() -> new Aprovada(criarOS(StatusOS.RECEBIDA), repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    void osRecebidaAtualizaParaAguardandoAprovacao() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);

        new OsRecebida(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.AGUARDANDO_APROVACAO, true);
    }

    @Test
    void osRecebidaRejeitaStatusInvalido() {
        assertThatThrownBy(() -> new OsRecebida(criarOS(StatusOS.EM_DIAGNOSTICO), repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    void entregarVeiculoMantemOSEntregue() {
        OrdemServico os = criarOS(StatusOS.ENTREGUE);

        new EntregarVeiculo(os, repository, eventPublisher);

        assertTransicao(os, StatusOS.ENTREGUE, false);
    }

    @Test
    void entregarVeiculoRejeitaStatusInvalido() {
        assertThatThrownBy(() -> new EntregarVeiculo(criarOS(StatusOS.FINALIZADA), repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    private OrdemServico criarOS(StatusOS status) {
        return new OrdemServico(status, BigDecimal.ZERO,
                List.of(), List.of(), List.of(), "Diagnóstico", null, null, null);
    }

    private void assertTransicao(OrdemServico os, StatusOS statusEsperado, boolean usaSaveAndFlush) {
        assertThat(os.getStatus()).isEqualTo(statusEsperado);
        verify(eventPublisher).publishEvent(any(OSCriadaEvent.class));
        if (usaSaveAndFlush) {
            verify(repository).saveAndFlush(os);
        } else {
            verify(repository).save(os);
        }
    }
}

package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.status.*;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EstadosOSTransitionTest {

    private OrdemServicoRepository repository;
    private ApplicationEventPublisher eventPublisher;
    private EstadosOS estadosOS;

    @BeforeEach
    void setUp() {
        repository = mock(OrdemServicoRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        estadosOS = new EstadosOS(repository, eventPublisher);
    }

    @ParameterizedTest
    @MethodSource("transicoes")
    void deveSelecionarEstadoEAvancar(StatusOS status, StatusOS step,
                                      Class<? extends AvancarStatusOSAbstract> tipoEsperado,
                                      StatusOS statusEsperado, boolean usaSave) {
        OrdemServico os = new OrdemServico(status, BigDecimal.ZERO,
                List.of(), List.of(), List.of(), "Diagnóstico", null, null, null);

        AvancarStatusOSAbstract resultado = estadosOS.executa(os, step);

        assertThat(resultado).isInstanceOf(tipoEsperado);
        assertThat(os.getStatus()).isEqualTo(statusEsperado);
        verify(eventPublisher).publishEvent(any(Object.class));
        if (usaSave) {
            verify(repository).save(os);
        } else {
            verify(repository).saveAndFlush(os);
        }
    }

    @ParameterizedTest
    @MethodSource("stepsDeAprovacao")
    void deveRejeitarTransicaoDeAprovacaoQuandoOSAindaNaoAguardaAprovacao(StatusOS step) {
        OrdemServico os = new OrdemServico(step, BigDecimal.ZERO,
                List.of(), List.of(), List.of(), "Diagnóstico", null, null, null);

        assertThatThrownBy(() -> estadosOS.executa(os, step))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("status aguardando aprovação");
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> transicoes() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(StatusOS.RECEBIDA, StatusOS.RECEBIDA, IniciaDiagnostico.class, StatusOS.EM_DIAGNOSTICO, true),
                org.junit.jupiter.params.provider.Arguments.of(StatusOS.EM_DIAGNOSTICO, StatusOS.EM_DIAGNOSTICO, AguardandoAprovacao.class, StatusOS.AGUARDANDO_APROVACAO, false),
                org.junit.jupiter.params.provider.Arguments.of(StatusOS.EM_EXECUCAO, StatusOS.EM_EXECUCAO, ExecutarServico.class, StatusOS.FINALIZADA, false),
                org.junit.jupiter.params.provider.Arguments.of(StatusOS.FINALIZADA, StatusOS.FINALIZADA, Finalizar.class, StatusOS.ENTREGUE, false),
                org.junit.jupiter.params.provider.Arguments.of(StatusOS.ENTREGUE, StatusOS.ENTREGUE, EntregarVeiculo.class, StatusOS.ENTREGUE, true));
    }

    private static Stream<StatusOS> stepsDeAprovacao() {
        return Stream.of(StatusOS.APROVADA, StatusOS.NEGADA);
    }
}

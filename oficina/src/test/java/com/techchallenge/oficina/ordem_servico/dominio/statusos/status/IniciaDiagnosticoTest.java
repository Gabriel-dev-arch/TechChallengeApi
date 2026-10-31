package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciaDiagnosticoTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void devePublicarEventoAntesDeAvancarDiagnostico() {
        OrdemServico os = new OrdemServico(StatusOS.RECEBIDA, BigDecimal.ZERO,
                List.of(), List.of(), List.of(), "Diagnostico", null, null, null);
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
            return null;
        }).when(eventPublisher).publishEvent(any(Object.class));

        new IniciaDiagnostico(os, repository, eventPublisher);

        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        verify(eventPublisher).publishEvent(any(OSCriadaEvent.class));
        verify(repository).save(os);
    }

    @Test
    void deveRejeitarStatusDiferenteDeRecebida() {
        OrdemServico os = new OrdemServico(StatusOS.EM_DIAGNOSTICO, BigDecimal.ZERO,
                List.of(), List.of(), List.of(), "Diagnostico", null, null, null);

        assertThatThrownBy(() -> new IniciaDiagnostico(os, repository, eventPublisher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }
}

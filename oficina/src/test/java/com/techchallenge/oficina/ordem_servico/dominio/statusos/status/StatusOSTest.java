package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusOSTest {

    @Mock
    private OrdemServicoRepository repository;

    private OrdemServico criarOS(StatusOS status) {
        return new OrdemServico(status, BigDecimal.ZERO, List.of(), List.of(), List.of(), "Diagnostico", null, null);
    }

    @Test
    @DisplayName("OsRecebida deve avançar de EM_DIAGNOSTICO para AGUARDANDO_APROVACAO com sucesso")
    void osRecebidaSucesso() {
        OrdemServico os = criarOS(StatusOS.EM_DIAGNOSTICO);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));

        OsRecebida osRecebida = new OsRecebida(os, repository);

        assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("OsRecebida deve lançar exceção se status não for EM_DIAGNOSTICO")
    void osRecebidaInvalido() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);

        assertThatThrownBy(() -> new OsRecebida(os, repository))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    @DisplayName("Aprovada deve avançar de APROVADA para EM_EXECUCAO")
    void aprovadaSucesso() {
        OrdemServico os = criarOS(StatusOS.APROVADA);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));

        Aprovada aprovada = new Aprovada(os, repository);

        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Aprovada deve lançar exceção se status não for APROVADA")
    void aprovadaInvalido() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);

        assertThatThrownBy(() -> new Aprovada(os, repository))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    @DisplayName("Finalizar deve avançar de FINALIZADA para ENTREGUE")
    void finalizarSucesso() {
        OrdemServico os = criarOS(StatusOS.FINALIZADA);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));

        Finalizar finalizar = new Finalizar(os, repository);

        assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Finalizar deve lançar exceção se status não for FINALIZADA")
    void finalizarInvalido() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);

        assertThatThrownBy(() -> new Finalizar(os, repository))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    @DisplayName("EmDiagnostico deve lançar exceção se status for diferente de RECEBIDA")
    void emDiagnosticoInvalido() {
        OrdemServico os = criarOS(StatusOS.EM_DIAGNOSTICO);

        assertThatThrownBy(() -> new EmDiagnostico(os, repository))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    @DisplayName("AguardandoAprovacao deve lançar exceção se status for diferente de EM_DIAGNOSTICO")
    void aguardandoAprovacaoInvalido() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);

        assertThatThrownBy(() -> new AguardandoAprovacao(os, repository))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    @DisplayName("EmExecucao deve lançar exceção se status for diferente de EM_EXECUCAO")
    void emExecucaoInvalido() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);

        assertThatThrownBy(() -> new EmExecucao(os, repository))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }
}

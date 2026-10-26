package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.status.*;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadosOSTest {

    @Mock
    private OrdemServicoRepository repository;

    private EstadosOS estadosOS;

    @BeforeEach
    void setUp() throws Exception {
        Constructor<EstadosOS> constructor = EstadosOS.class.getDeclaredConstructor(OrdemServicoRepository.class);
        constructor.setAccessible(true);
        estadosOS = constructor.newInstance(repository);
    }

    private OrdemServico criarOS(StatusOS status) {
        return new OrdemServico(status, BigDecimal.ZERO, List.of(), List.of(), List.of(), "Diagnostico", null, null);
    }

    @Test
    @DisplayName("Deve transitar RECEBIDA para EmDiagnostico e atualizar status para EM_DIAGNOSTICO")
    void deveExecutarRecebida() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvancarStatus resultado = estadosOS.executa(os);

        assertThat(resultado).isInstanceOf(EmDiagnostico.class);
        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve transitar EM_DIAGNOSTICO para AguardandoAprovacao e atualizar status para AGUARDANDO_APROVACAO")
    void deveExecutarEmDiagnostico() {
        OrdemServico os = criarOS(StatusOS.EM_DIAGNOSTICO);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvancarStatus resultado = estadosOS.executa(os);

        assertThat(resultado).isInstanceOf(AguardandoAprovacao.class);
        assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve transitar APROVADA para AprovarOuNegar e atualizar status para EM_EXECUCAO")
    void deveExecutarAprovada() {
        OrdemServico os = criarOS(StatusOS.APROVADA);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvancarStatus resultado = estadosOS.executa(os);

        assertThat(resultado).isInstanceOf(AprovarOuNegar.class);
        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve transitar NEGADA para AprovarOuNegar e atualizar status para FINALIZADA")
    void deveExecutarNegada() {
        OrdemServico os = criarOS(StatusOS.NEGADA);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvancarStatus resultado = estadosOS.executa(os);

        assertThat(resultado).isInstanceOf(AprovarOuNegar.class);
        assertThat(os.getStatus()).isEqualTo(StatusOS.FINALIZADA);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve transitar EM_EXECUCAO para EmExecucao e atualizar status para ENTREGUE")
    void deveExecutarEmExecucao() {
        OrdemServico os = criarOS(StatusOS.EM_EXECUCAO);
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvancarStatus resultado = estadosOS.executa(os);

        assertThat(resultado).isInstanceOf(EmExecucao.class);
        assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve transitar ENTREGUE para Finalizar")
    void deveExecutarEntregue() {
        OrdemServico os = criarOS(StatusOS.ENTREGUE);

        // Finalizar exige status FINALIZADA na validação do construtor
        assertThatThrownBy(() -> estadosOS.executa(os))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    @DisplayName("Deve lançar exceção para status inesperado")
    void deveLancarExcecaoParaStatusInesperado() {
        OrdemServico os = criarOS(StatusOS.FINALIZADA);

        assertThatThrownBy(() -> estadosOS.executa(os))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unexpected value: " + StatusOS.FINALIZADA);
    }
}

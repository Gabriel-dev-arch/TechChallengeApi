package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.funcionario.dominio.FuncionarioNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    @DisplayName("OrdemServicoNaoEncontradaException por UUID e Placa")
    void testOrdemServicoNaoEncontradaException() {
        UUID id = UUID.randomUUID();
        OrdemServicoNaoEncontradaException ex1 = new OrdemServicoNaoEncontradaException(id);
        assertThat(ex1.getMessage()).contains(id.toString());

        OrdemServicoNaoEncontradaException ex2 = new OrdemServicoNaoEncontradaException("ABC1234");
        assertThat(ex2.getMessage()).contains("ABC1234");
    }

    @Test
    @DisplayName("VeiculoNaoEncontradoException por UUID e Placa")
    void testVeiculoNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        VeiculoNaoEncontradoException ex1 = new VeiculoNaoEncontradoException(id);
        assertThat(ex1.getMessage()).contains(id.toString());

        VeiculoNaoEncontradoException ex2 = new VeiculoNaoEncontradoException("ABC1234");
        assertThat(ex2.getMessage()).contains("ABC1234");
    }

    @Test
    @DisplayName("OrdemServicoEmAbertoException por UUID e Placa")
    void testOrdemServicoEmAbertoException() {
        UUID id = UUID.randomUUID();
        OrdemServicoEmAbertoException ex1 = new OrdemServicoEmAbertoException(id);
        assertThat(ex1.getMessage()).contains(id.toString());

        OrdemServicoEmAbertoException ex2 = new OrdemServicoEmAbertoException("ABC1234");
        assertThat(ex2.getMessage()).contains("ABC1234");
    }

    @Test
    @DisplayName("FuncionarioNaoEncontradoException informa o UUID")
    void testFuncionarioNaoEncontradoException() {
        UUID id = UUID.randomUUID();

        assertThat(new FuncionarioNaoEncontradoException(id).getMessage())
                .isEqualTo("Funcionario não encontrado: " + id);
    }

    @Test
    @DisplayName("TransicaoInvalidaException preserva a mensagem")
    void testTransicaoInvalidaException() {
        assertThat(new TransicaoInvalidaException("Status não permite a transição").getMessage())
                .isEqualTo("Status não permite a transição");
    }
}

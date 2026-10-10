package com.techchallenge.oficina.servicos.atualizar;

import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.entidades.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarServicoServiceTest {

    private static final UUID ID = UUID.fromString("11111111-2222-3333-4444-555555666666");

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private AtualizarServicoService service;

    @Test
    void deveAtualizarServicoExistente() {
        Servico servico = new Servico("Lavagem simples", new BigDecimal("80.00"));
        AtualizarServicoRequest request = new AtualizarServicoRequest("Lavagem completa", new BigDecimal("120.00"));

        when(repository.findById(ID)).thenReturn(Optional.of(servico));
        when(repository.saveAndFlush(servico)).thenReturn(servico);

        ServicoResponse response = service.atualizar(ID, request);

        assertThat(response.descricao()).isEqualTo("Lavagem completa");
        assertThat(response.valor()).isEqualByComparingTo(new BigDecimal("120.00"));
        verify(repository).saveAndFlush(servico);
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoExiste() {
        AtualizarServicoRequest request = new AtualizarServicoRequest("Lavagem completa", new BigDecimal("120.00"));

        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(ID, request))
                .isInstanceOf(ServicoNaoEncontradoException.class)
                .hasMessage("Servico não encontrado: " + ID);
    }
}

package com.techchallenge.oficina.servicos.remover;

import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoverServicoServiceTest {

    private static final UUID ID = UUID.fromString("55555555-6666-7777-8888-999999000000");

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private RemoverServicoService service;

    @Test
    void deveRemoverServicoExistente() {
        when(repository.existsById(ID)).thenReturn(true);

        service.remover(ID);

        verify(repository).deleteById(ID);
        verify(repository).flush();
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoExiste() {
        when(repository.existsById(ID)).thenReturn(false);

        assertThatThrownBy(() -> service.remover(ID))
                .isInstanceOf(ServicoNaoEncontradoException.class)
                .hasMessage("Servico não encontrado: " + ID);

        verify(repository, never()).deleteById(ID);
        verify(repository, never()).flush();
    }
}

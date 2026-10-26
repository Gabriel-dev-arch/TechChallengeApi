package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteAprovaRecusaOSServiceTest {

    @Mock
    private OrdemServicoRepository repository;

    @InjectMocks
    private ClienteAprovaRecusaOSService service;

    private final UUID id = UUID.randomUUID();

    private OrdemServico criarOS(StatusOS status) {
        return new OrdemServico(status, BigDecimal.ZERO, List.of(), List.of(), List.of(), "Diagnostico", null, null);
    }

    @Test
    @DisplayName("Deve aprovar OS existente com sucesso")
    void deveAprovarOSExistente() {
        OrdemServico os = criarOS(StatusOS.AGUARDANDO_APROVACAO);
        when(repository.findById(id)).thenReturn(Optional.of(os));

        service.aprovaOS(id);

        assertThat(os.getStatus()).isEqualTo(StatusOS.APROVADA);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve lançar exceção ao aprovar OS inexistente")
    void deveLancarExcecaoAoAprovarOSInexistente() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aprovaOS(id))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve recusar OS existente com sucesso")
    void deveRecusarOSExistente() {
        OrdemServico os = criarOS(StatusOS.AGUARDANDO_APROVACAO);
        when(repository.findById(id)).thenReturn(Optional.of(os));

        service.recusaOS(id);

        assertThat(os.getStatus()).isEqualTo(StatusOS.NEGADA);
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve lançar exceção ao recusar OS inexistente")
    void deveLancarExcecaoAoRecusarOSInexistente() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recusaOS(id))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(repository, never()).saveAndFlush(any());
    }
}

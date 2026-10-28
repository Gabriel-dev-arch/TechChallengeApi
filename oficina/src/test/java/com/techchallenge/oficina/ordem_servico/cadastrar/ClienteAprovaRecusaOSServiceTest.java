package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.clientes.entidades.Cliente;
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
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteAprovaRecusaOSServiceTest {

    private static final String DOCUMENTO = "52998224725";

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private ControleDeAcesso controleDeAcesso;

    @InjectMocks
    private ClienteAprovaRecusaOSService service;

    private final UUID id = UUID.randomUUID();

    private OrdemServico criarOS(StatusOS status) {
        Cliente cliente = new Cliente("Maria", "Silva", "maria@email.com", DOCUMENTO, null);
        return new OrdemServico(status, BigDecimal.ZERO, List.of(), List.of(), List.of(), "Diagnostico", null, cliente, null);
    }

    @Test
    @DisplayName("Deve aprovar OS existente com sucesso")
    void deveAprovarOSExistente() {
        OrdemServico os = criarOS(StatusOS.AGUARDANDO_APROVACAO);
        when(repository.findById(id)).thenReturn(Optional.of(os));

        service.aprovaOS(id);

        assertThat(os.getStatus()).isEqualTo(StatusOS.APROVADA);
        verify(controleDeAcesso).verificarDono(DOCUMENTO);
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
    @DisplayName("Não deve aprovar OS de outro cliente")
    void naoDeveAprovarOSDeOutroCliente() {
        OrdemServico os = criarOS(StatusOS.AGUARDANDO_APROVACAO);
        when(repository.findById(id)).thenReturn(Optional.of(os));
        doThrow(new AccessDeniedException("Acesso negado")).when(controleDeAcesso).verificarDono(DOCUMENTO);

        assertThatThrownBy(() -> service.aprovaOS(id))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve recusar OS existente com sucesso")
    void deveRecusarOSExistente() {
        OrdemServico os = criarOS(StatusOS.AGUARDANDO_APROVACAO);
        when(repository.findById(id)).thenReturn(Optional.of(os));

        service.recusaOS(id);

        assertThat(os.getStatus()).isEqualTo(StatusOS.NEGADA);
        verify(controleDeAcesso).verificarDono(DOCUMENTO);
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

    @Test
    @DisplayName("Não deve recusar OS de outro cliente")
    void naoDeveRecusarOSDeOutroCliente() {
        OrdemServico os = criarOS(StatusOS.AGUARDANDO_APROVACAO);
        when(repository.findById(id)).thenReturn(Optional.of(os));
        doThrow(new AccessDeniedException("Acesso negado")).when(controleDeAcesso).verificarDono(DOCUMENTO);

        assertThatThrownBy(() -> service.recusaOS(id))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
        verify(repository, never()).saveAndFlush(any());
    }
}

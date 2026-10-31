package com.techchallenge.oficina.ordem_servico.finalizar;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.EstadosOS;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.dominio.*;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalizarOSServiceTest {

    @Mock OrdemServicoRepository repository;
    @Mock EntityManager entityManager;
    @Mock ApplicationEventPublisher publisher;

    private FinalizarOSService service() {
        return new FinalizarOSService(repository, entityManager, new EstadosOS(repository, publisher));
    }

    private PecaInsumo item(TipoItem tipo, String id) {
        var peca = new PecaInsumo(tipo, id, id, null, UnidadeMedida.L, BigDecimal.TEN, new BigDecimal("10"));
        ReflectionTestUtils.setField(peca, "id", UUID.fromString(id));
        return peca;
    }

    private OrdemServico os(StatusOS status, List<PecaInsumo> itens, BigDecimal quantidade) {
        var cliente = new Cliente("Maria", "Silva", "maria@email.com", "52998224725", null);
        return new OrdemServico(status, BigDecimal.ZERO, List.of(), itens,
                itens.stream().map(item -> new ItemQuantidade(item.getId(), quantidade)).toList(), null,
                new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente), cliente,
                new Funcionario("Mecânico", "mecanico@email.com", (short) 1, false, true));
    }

    @Test
    void finalizaPecasEInsumosPreservandoReservasDeOutrasOrdens() {
        var peca = item(TipoItem.PECA, "00000000-0000-0000-0000-000000000001");
        var insumo = item(TipoItem.INSUMO, "00000000-0000-0000-0000-000000000002");
        peca.reservar(new BigDecimal("4"));
        insumo.reservar(new BigDecimal("2.125"));
        var os = os(StatusOS.EM_EXECUCAO, List.of(insumo, peca), new BigDecimal("2.125"));
        UUID id = UUID.randomUUID();
        when(repository.buscarParaFinalizar(id)).thenReturn(Optional.of(os));

        var response = service().finalizar(id);

        assertThat(os.getStatus()).isEqualTo(StatusOS.FINALIZADA);
        assertThat(response.orcamento().status()).isEqualTo(StatusOS.FINALIZADA.getDescricao());
        assertThat(peca.getQuantidadeTotal()).isEqualByComparingTo("7.875");
        assertThat(peca.getQuantidadeReservada()).isEqualByComparingTo("1.875");
        assertThat(insumo.getQuantidadeTotal()).isEqualByComparingTo("7.875");
        assertThat(insumo.getQuantidadeReservada()).isZero();
        var ordem = inOrder(entityManager, repository);
        ordem.verify(entityManager).refresh(peca, LockModeType.PESSIMISTIC_WRITE);
        ordem.verify(entityManager).refresh(insumo, LockModeType.PESSIMISTIC_WRITE);
        ordem.verify(repository).saveAndFlush(os);
        verify(publisher).publishEvent(any(Object.class));
    }

    @Test
    void finalizaSemItens() {
        var os = os(StatusOS.EM_EXECUCAO, List.of(), BigDecimal.ONE);
        UUID id = UUID.randomUUID();
        when(repository.buscarParaFinalizar(id)).thenReturn(Optional.of(os));
        service().finalizar(id);
        assertThat(os.getStatus()).isEqualTo(StatusOS.FINALIZADA);
        verifyNoInteractions(entityManager);
    }

    @ParameterizedTest
    @EnumSource(value = StatusOS.class, names = {"FINALIZADA", "ENTREGUE"})
    void repeticaoNaoBaixaNemAvancaStatus(StatusOS status) {
        var os = os(status, List.of(), BigDecimal.ONE);
        UUID id = UUID.randomUUID();
        when(repository.buscarParaFinalizar(id)).thenReturn(Optional.of(os));
        service().finalizar(id);
        assertThat(os.getStatus()).isEqualTo(status);
        verifyNoInteractions(entityManager, publisher);
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusOS.class, names = {"EM_EXECUCAO", "FINALIZADA", "ENTREGUE"}, mode = EnumSource.Mode.EXCLUDE)
    void impedeFinalizacaoEmStatusInvalido(StatusOS status) {
        var os = os(status, List.of(), BigDecimal.ONE);
        UUID id = UUID.randomUUID();
        when(repository.buscarParaFinalizar(id)).thenReturn(Optional.of(os));
        assertThatThrownBy(() -> service().finalizar(id)).isInstanceOf(TransicaoInvalidaException.class);
        assertThat(os.getStatus()).isEqualTo(status);
        verifyNoInteractions(entityManager, publisher);
    }

    @Test
    void inexistenteRetornaErro() {
        assertThatThrownBy(() -> service().finalizar(UUID.randomUUID()))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
        verifyNoInteractions(entityManager, publisher);
    }

    @Test
    void saldoInsuficienteNaoAlteraNenhumItemNemStatus() {
        var peca = item(TipoItem.PECA, "00000000-0000-0000-0000-000000000001");
        var insumo = item(TipoItem.INSUMO, "00000000-0000-0000-0000-000000000002");
        peca.reservar(BigDecimal.ONE);
        insumo.reservar(BigDecimal.ONE);
        ReflectionTestUtils.setField(insumo, "quantidadeTotal", BigDecimal.ZERO);
        var os = os(StatusOS.EM_EXECUCAO, List.of(peca, insumo), BigDecimal.ONE);
        UUID id = UUID.randomUUID();
        when(repository.buscarParaFinalizar(id)).thenReturn(Optional.of(os));
        assertThatThrownBy(() -> service().finalizar(id)).isInstanceOf(EstoqueInsuficienteException.class);
        assertThat(peca.getQuantidadeTotal()).isEqualByComparingTo("10");
        assertThat(peca.getQuantidadeReservada()).isEqualByComparingTo("1");
        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void reservaInsuficienteImpedeConsumoDeEstoqueDeOutraOS() {
        var peca = item(TipoItem.PECA, "00000000-0000-0000-0000-000000000001");
        var os = os(StatusOS.EM_EXECUCAO, List.of(peca), BigDecimal.ONE);
        UUID id = UUID.randomUUID();
        when(repository.buscarParaFinalizar(id)).thenReturn(Optional.of(os));
        assertThatThrownBy(() -> service().finalizar(id)).isInstanceOf(ReservaInsuficienteException.class);
        assertThat(peca.getQuantidadeTotal()).isEqualByComparingTo("10");
        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
    }
}

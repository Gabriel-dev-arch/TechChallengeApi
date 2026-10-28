package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrcamentoFechadoException;
import com.techchallenge.oficina.pecasinsumos.dominio.EstoqueInsuficienteException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoInativoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.QuantidadeInvalidaException;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdemServicoTest {

    private void setId(Object entity, UUID id) {
        try {
            var field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private void setCreatedAt(BaseEntity entity, Instant instant) {
        try {
            var field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(entity, instant);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("Deve formatar data corretamente quando createdAt presente e quando nulo")
    void deveFormatarData() {
        Cliente cliente = new Cliente("João", "Silva", "joao@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente);
        OrdemServico os = new OrdemServico(StatusOS.RECEBIDA, veiculo, cliente, null);

        assertThat(os.formatDate()).isNull();

        setCreatedAt(os, Instant.parse("2026-10-25T14:30:00Z"));
        // No fuso -03:00, 14:30Z é 11:30
        assertThat(os.formatDate()).contains("25/10/2026 11:30");
    }

    @Test
    @DisplayName("Deve incluir serviços e somar ao orçamento")
    void deveIncluirServicos() {
        Cliente cliente = new Cliente("João", "Silva", "joao@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente);
        OrdemServico os = new OrdemServico(StatusOS.RECEBIDA, BigDecimal.valueOf(100), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "Diagnostico", veiculo, cliente, null);

        Servico s1 = new Servico("Alinhamento", BigDecimal.valueOf(80));
        Servico s2 = new Servico("Balanceamento", BigDecimal.valueOf(60));

        os.incluirServicos(List.of(s1, s2));

        assertThat(os.getServicos()).containsExactly(s1, s2);
        assertThat(os.getOrcamento()).isEqualByComparingTo(BigDecimal.valueOf(240));
    }

    @Test
    @DisplayName("Deve incluir peças/insumos novos e existentes somando quantidade e valor ao orçamento")
    void deveIncluirPecasEInsumos() {
        Cliente cliente = new Cliente("João", "Silva", "joao@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente);

        UUID pecaId = UUID.randomUUID();
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "VELA", "Vela", "Vela de ignição",
                UnidadeMedida.UN, BigDecimal.valueOf(25), BigDecimal.TEN);
        setId(peca, pecaId);

        OrdemServico os = new OrdemServico(StatusOS.RECEBIDA, BigDecimal.ZERO, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "Diagnostico", veiculo, cliente, null);

        // Adiciona 2 velas
        os.incluirPecaInsumos(List.of(peca), List.of(new ItemQuantidade(pecaId, BigDecimal.valueOf(2))));
        assertThat(os.getItensQuantidade()).hasSize(1);
        assertThat(os.getItensQuantidade().getFirst().getQuantidade()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(os.getOrcamento()).isEqualByComparingTo(BigDecimal.valueOf(50));

        // Adiciona mais 2 velas (item existente)
        os.incluirPecaInsumos(List.of(peca), List.of(new ItemQuantidade(pecaId, BigDecimal.valueOf(2))));
        assertThat(os.getItensQuantidade()).hasSize(1);
        assertThat(os.getItensQuantidade().getFirst().getQuantidade()).isEqualByComparingTo(BigDecimal.valueOf(4));
        assertThat(os.getOrcamento()).isEqualByComparingTo(BigDecimal.valueOf(100));

        assertThat(os.getItens()).containsExactly(peca);
    }

    @Test
    @DisplayName("Deve montar diagnóstico com base nos serviços e itens incluídos")
    void deveIncluirDiagnostico() {
        Cliente cliente = new Cliente("João", "Silva", "joao@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente);

        UUID pecaId = UUID.randomUUID();
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "OLEO", "Óleo", "Troca do fluido",
                UnidadeMedida.UN, BigDecimal.valueOf(30), BigDecimal.TEN);
        setId(peca, pecaId);

        Servico servico = new Servico("Mão de obra", BigDecimal.valueOf(100));

        OrdemServico os = new OrdemServico(StatusOS.RECEBIDA, BigDecimal.ZERO,
                new ArrayList<>(List.of(servico)),
                List.of(peca),
                List.of(new ItemQuantidade(pecaId, BigDecimal.ONE)),
                null, veiculo, cliente, null);

        os.incluirDiagnostico();

        assertThat(os.getDiagnostico()).contains("Mão de obra");
        assertThat(os.getDiagnostico()).contains("Troca do fluido");
    }

    @Test
    @DisplayName("OrdemServicoItem deve adicionar quantidade corretamente")
    void ordemServicoItemAdicionarQuantidade() {
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "FILTRO", "Filtro", "Filtro de ar",
                UnidadeMedida.UN, BigDecimal.valueOf(40), BigDecimal.TEN);
        OrdemServicoItem item = new OrdemServicoItem(peca, BigDecimal.valueOf(3));

        assertThat(item.getItem()).isEqualTo(peca);
        assertThat(item.getQuantidade()).isEqualByComparingTo(BigDecimal.valueOf(3));

        item.adicionarQuantidade(BigDecimal.valueOf(2));
        assertThat(item.getQuantidade()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @ParameterizedTest
    @EnumSource(value = StatusOS.class, names = {"RECEBIDA", "EM_DIAGNOSTICO"})
    @DisplayName("Deve aceitar serviços e peças enquanto o orçamento está aberto")
    void deveAceitarItensComOrcamentoAberto(StatusOS status) {
        OrdemServico os = criarOS(status);
        PecaInsumo peca = criarPeca();

        os.incluirServicos(List.of(new Servico("Alinhamento", BigDecimal.valueOf(80))));
        os.incluirPecaInsumos(List.of(peca), List.of(new ItemQuantidade(peca.getId(), BigDecimal.ONE)));

        assertThat(os.getOrcamento()).isEqualByComparingTo(BigDecimal.valueOf(105));
    }

    @ParameterizedTest
    @EnumSource(value = StatusOS.class, names = {"RECEBIDA", "EM_DIAGNOSTICO"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Não deve alterar serviços nem peças depois que o orçamento foi enviado ao cliente")
    void naoDeveAlterarOrcamentoFechado(StatusOS status) {
        OrdemServico os = criarOS(status);
        PecaInsumo peca = criarPeca();
        List<Servico> servicos = List.of(new Servico("Alinhamento", BigDecimal.valueOf(80)));
        List<ItemQuantidade> itens = List.of(new ItemQuantidade(peca.getId(), BigDecimal.ONE));

        assertThatThrownBy(() -> os.incluirServicos(servicos)).isInstanceOf(OrcamentoFechadoException.class);
        assertThatThrownBy(() -> os.incluirPecaInsumos(List.of(peca), itens)).isInstanceOf(OrcamentoFechadoException.class);

        assertThat(os.getServicos()).isEmpty();
        assertThat(os.getItensQuantidade()).isEmpty();
        assertThat(os.getOrcamento()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-3", "-0.5"})
    @DisplayName("Deve rejeitar quantidade nula, zero ou negativa sem alterar a OS")
    void deveRejeitarQuantidadeInvalida(BigDecimal quantidade) {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();
        List<ItemQuantidade> itens = List.of(
                new ItemQuantidade(peca.getId(), BigDecimal.ONE),
                new ItemQuantidade(peca.getId(), quantidade));

        assertThatThrownBy(() -> os.incluirPecaInsumos(List.of(peca), itens))
                .isInstanceOf(QuantidadeInvalidaException.class);

        assertThat(os.getItensQuantidade()).isEmpty();
        assertThat(os.getOrcamento()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Incluir peça reserva a quantidade no estoque")
    void deveReservarEstoqueAoIncluirPeca() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();

        os.incluirPecaInsumos(List.of(peca), List.of(new ItemQuantidade(peca.getId(), BigDecimal.valueOf(3))));

        assertThat(peca.getQuantidadeReservada()).isEqualByComparingTo("3");
        assertThat(peca.getQuantidadeDisponivel()).isEqualByComparingTo("7");
    }

    @Test
    @DisplayName("Peça inexistente na inclusão devolve PecaInsumoNaoEncontradoException")
    void deveRejeitarPecaInexistente() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();
        List<ItemQuantidade> itens = List.of(
                new ItemQuantidade(peca.getId(), BigDecimal.ONE),
                new ItemQuantidade(UUID.randomUUID(), BigDecimal.ONE));

        assertThatThrownBy(() -> os.incluirPecaInsumos(List.of(peca), itens))
                .isInstanceOf(PecaInsumoNaoEncontradoException.class);

        assertThat(os.getItensQuantidade()).isEmpty();
        assertThat(peca.getQuantidadeReservada()).isZero();
    }

    @Test
    @DisplayName("Não deve incluir quantidade acima do disponível no estoque")
    void deveRejeitarQuantidadeAcimaDoEstoque() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();
        List<ItemQuantidade> itens = List.of(new ItemQuantidade(peca.getId(), BigDecimal.valueOf(11)));

        assertThatThrownBy(() -> os.incluirPecaInsumos(List.of(peca), itens))
                .isInstanceOf(EstoqueInsuficienteException.class);
    }

    @Test
    @DisplayName("Não deve incluir peça removida (inativa)")
    void deveRejeitarPecaInativa() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();
        peca.desativar();
        List<ItemQuantidade> itens = List.of(new ItemQuantidade(peca.getId(), BigDecimal.ONE));

        assertThatThrownBy(() -> os.incluirPecaInsumos(List.of(peca), itens))
                .isInstanceOf(PecaInsumoInativoException.class);
    }

    @Test
    @DisplayName("Não deve incluir quantidade fracionada de peça vendida por unidade")
    void deveRejeitarQuantidadeFracionadaEmUnidade() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();
        List<ItemQuantidade> itens = List.of(new ItemQuantidade(peca.getId(), new BigDecimal("1.5")));

        assertThatThrownBy(() -> os.incluirPecaInsumos(List.of(peca), itens))
                .isInstanceOf(QuantidadeInvalidaException.class);
    }

    @Test
    @DisplayName("Liberar reservas devolve ao estoque o que estava reservado para a OS")
    void deveLiberarReservas() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();
        os.incluirPecaInsumos(List.of(peca), List.of(new ItemQuantidade(peca.getId(), BigDecimal.valueOf(3))));

        os.liberarReservas();

        assertThat(peca.getQuantidadeReservada()).isZero();
        assertThat(peca.getQuantidadeTotal()).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("Consumir reservas dá baixa no estoque das peças da OS")
    void deveConsumirReservas() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        PecaInsumo peca = criarPeca();
        os.incluirPecaInsumos(List.of(peca), List.of(new ItemQuantidade(peca.getId(), BigDecimal.valueOf(3))));

        os.consumirReservas();

        assertThat(peca.getQuantidadeReservada()).isZero();
        assertThat(peca.getQuantidadeTotal()).isEqualByComparingTo("7");
    }

    private OrdemServico criarOS(StatusOS status) {
        Cliente cliente = new Cliente("João", "Silva", "joao@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente);
        return new OrdemServico(status, BigDecimal.ZERO, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                null, veiculo, cliente, null);
    }

    private PecaInsumo criarPeca() {
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "VELA", "Vela", "Vela de ignição",
                UnidadeMedida.UN, BigDecimal.valueOf(25), BigDecimal.TEN);
        setId(peca, UUID.randomUUID());
        return peca;
    }
}

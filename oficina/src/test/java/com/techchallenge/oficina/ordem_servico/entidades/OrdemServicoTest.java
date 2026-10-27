package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
}

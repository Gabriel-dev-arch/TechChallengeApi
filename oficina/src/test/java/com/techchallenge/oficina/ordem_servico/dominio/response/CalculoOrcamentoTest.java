package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.ordem_servico.cadastrar.ItemQuantidade;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CalculoOrcamentoTest {

    @Test
    void deveSomarServicosEItensPelaQuantidade() {
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "FILTRO", "Filtro", "Filtro de óleo",
                UnidadeMedida.UN, new BigDecimal("25.50"), new BigDecimal("5"));
        setId(peca, UUID.randomUUID());
        List<ItemQuantidade> itens = List.of(new ItemQuantidade(peca.getId(), new BigDecimal("2")));
        List<Servico> servicos = List.of(
                new Servico("Troca", new BigDecimal("30.00")),
                new Servico("Inspeção", new BigDecimal("15.00")));

        BigDecimal total = CalculoOrcamento.realizaCalculoTotal(List.of(peca), itens, servicos);

        assertThat(total).isEqualByComparingTo("96.00");
    }

    @Test
    void deveRetornarZeroQuandoNaoHaServicosNemItens() {
        assertThat(CalculoOrcamento.realizaCalculoTotal(List.of(), List.of(), List.of()))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    private void setId(PecaInsumo peca, UUID id) {
        try {
            var field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(peca, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Não foi possível configurar o id da peça", e);
        }
    }
}

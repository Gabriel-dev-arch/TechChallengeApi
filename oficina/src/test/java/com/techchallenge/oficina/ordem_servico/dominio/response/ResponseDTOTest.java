package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDTOTest {

    private void setId(Object entity, UUID id) {
        try {
            var field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("PecaInsumoResponse.from deve mapear corretamente a entidade")
    void testPecaInsumoResponse() throws Exception {
        PecaInsumo item = new PecaInsumo(TipoItem.PECA, "COD1", "Nome", "Desc", UnidadeMedida.UN, BigDecimal.TEN, BigDecimal.ONE);
        PecaInsumoResponse response = PecaInsumoResponse.from(item);

        assertThat(response.codigo()).isEqualTo("COD1");
        assertThat(response.tipo()).isEqualTo(TipoItem.PECA);
        assertThat(response.nome()).isEqualTo("Nome");
        assertThat(response.descricao()).isEqualTo("Desc");
        assertThat(response.unidadeMedida()).isEqualTo(UnidadeMedida.UN);
        assertThat(response.precoUnitario()).isEqualByComparingTo(BigDecimal.TEN);

        Method paraFuso = PecaInsumoResponse.class.getDeclaredMethod("paraFusoLocal", Instant.class);
        paraFuso.setAccessible(true);
        assertThat(paraFuso.invoke(null, (Instant) null)).isNull();
        Instant agora = Instant.now();
        OffsetDateTime offsetDateTime = (OffsetDateTime) paraFuso.invoke(null, agora);
        assertThat(offsetDateTime).isNotNull();
    }

    @Test
    @DisplayName("VeiculoResponse.from deve mapear corretamente")
    void testVeiculoResponse() throws Exception {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente);

        VeiculoResponse response = VeiculoResponse.from(veiculo);
        assertThat(response.placa()).isEqualTo("ABC1234");
        assertThat(response.marca()).isEqualTo("Fiat");
        assertThat(response.modelo()).isEqualTo("Uno");
        assertThat(response.ano()).isEqualTo(2020);

        Method paraFuso = VeiculoResponse.class.getDeclaredMethod("paraFusoLocal", Instant.class);
        paraFuso.setAccessible(true);
        assertThat(paraFuso.invoke(null, (Instant) null)).isNull();
        Instant agora = Instant.now();
        OffsetDateTime offsetDateTime = (OffsetDateTime) paraFuso.invoke(null, agora);
        assertThat(offsetDateTime).isNotNull();
    }

    @Test
    @DisplayName("OrdemServicoResponse deve mapear a entidade com e sem status explícito")
    void testOrdemServicoResponse() {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente);
        Servico servico = new Servico("Revisão", new BigDecimal("100.00"));
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "COD", "Vela", "Vela", UnidadeMedida.UN, BigDecimal.TEN, BigDecimal.ONE);
        UUID pecaId = UUID.randomUUID();
        setId(peca, pecaId);

        OrdemServico os = new OrdemServico(StatusOS.RECEBIDA, BigDecimal.valueOf(110),
                List.of(servico), List.of(peca), List.of(new ItemQuantidade(pecaId, BigDecimal.ONE)),
                "Diagnóstico", veiculo, cliente,
                new Funcionario("Ana Silva", "ana.silva@empresa.com", (short) 1234, false, true));
        setId(os, UUID.randomUUID());

        OrdemServicoResponse r1 = OrdemServicoResponse.from(os);
        assertThat(r1.id()).isEqualTo(os.getId());
        assertThat(r1.orcamento().status()).isEqualTo(StatusOS.RECEBIDA.getDescricao());

        OrdemServicoResponse r2 = OrdemServicoResponse.from(os, StatusOS.FINALIZADA);
        assertThat(r2.orcamento().status()).isEqualTo(StatusOS.FINALIZADA.getDescricao());
    }
}

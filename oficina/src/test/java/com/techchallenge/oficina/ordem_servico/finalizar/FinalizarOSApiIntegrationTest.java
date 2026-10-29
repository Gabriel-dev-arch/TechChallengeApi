package com.techchallenge.oficina.ordem_servico.finalizar;

import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.funcionario.dominio.FuncionarioRepository;
import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.EstadosOS;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.dominio.*;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.support.PostgresTestConfiguration;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.application.name=oficina-finalizacao-test")
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@WithMockUser(roles = "ADMIN")
class FinalizarOSApiIntegrationTest {

    private static final AtomicInteger CONTADOR = new AtomicInteger();

    @Autowired FinalizarOSService service;
    @Autowired OrdemServicoRepository repository;
    @Autowired ClienteRepository clientes;
    @Autowired VeiculoRepository veiculos;
    @Autowired FuncionarioRepository funcionarios;
    @Autowired PecaInsumoRepository estoque;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired MockMvc mockMvc;
    @MockitoSpyBean EstadosOS estadosOS;

    private record Dados(List<UUID> ordens, UUID peca, UUID insumo) {}

    private Dados preparar(int numeroOrdens) {
        return new TransactionTemplate(transactionManager).execute(tx -> {
            int contador = CONTADOR.incrementAndGet();
            var peca = estoque.saveAndFlush(new PecaInsumo(TipoItem.PECA, "P-" + contador, "Filtro", null,
                    UnidadeMedida.UN, BigDecimal.TEN, BigDecimal.TEN));
            var insumo = estoque.saveAndFlush(new PecaInsumo(TipoItem.INSUMO, "I-" + contador, "Óleo", null,
                    UnidadeMedida.L, BigDecimal.TEN, BigDecimal.TEN));
            var funcionario = funcionarios.saveAndFlush(new Funcionario("Mecânico", "mecanico@email.com", (short) 1, false, true));
            var ids = new ArrayList<UUID>();
            for (int i = 0; i < numeroOrdens; i++) {
                int numero = CONTADOR.incrementAndGet();
                var cliente = clientes.saveAndFlush(new Cliente("Maria", "Silva", "maria@email.com",
                        String.format("%011d", numero), null));
                var veiculo = veiculos.saveAndFlush(new Veiculo(String.format("TST%04d", numero), "Fiat", "Uno", 2020, cliente));
                var os = new OrdemServico(StatusOS.RECEBIDA, BigDecimal.ZERO, new ArrayList<>(), List.of(), List.of(),
                        null, veiculo, cliente, funcionario);
                os.incluirPecaInsumos(List.of(peca, insumo), List.of(new ItemQuantidade(peca.getId(), BigDecimal.ONE),
                        new ItemQuantidade(insumo.getId(), new BigDecimal("1.125"))));
                os.atualizaStatus(StatusOS.EM_EXECUCAO);
                ids.add(repository.saveAndFlush(os).getId());
            }
            return new Dados(ids, peca.getId(), insumo.getId());
        });
    }

    private BigDecimal saldo(UUID item, String coluna) {
        return jdbcTemplate.queryForObject("select " + coluna + " from pecas_insumos where id = ?", BigDecimal.class, item);
    }

    private String statusDaOS(UUID os) {
        return jdbcTemplate.queryForObject("select status from ordem_servico where id = ?", String.class, os);
    }

    @Test
    void endpointFinalizaUmaVezEPreservaDadosDoCliente() throws Exception {
        var dados = preparar(1);
        String rota = "/ordens-servico/" + dados.ordens().getFirst() + "/finalizar";
        mockMvc.perform(post(rota)).andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento.status").value(StatusOS.FINALIZADA.getDescricao()))
                .andExpect(jsonPath("$.cliente.senha").doesNotExist())
                .andExpect(jsonPath("$.cliente.senhaHash").doesNotExist());
        mockMvc.perform(post(rota)).andExpect(status().isOk());
        assertThat(saldo(dados.peca(), "quantidade_total")).isEqualByComparingTo("9");
        assertThat(saldo(dados.insumo(), "quantidade_total")).isEqualByComparingTo("8.875");
        assertThat(saldo(dados.insumo(), "quantidade_reservada")).isZero();
    }

    @Test
    void reservaInsuficienteRetorna409SemBaixasParciais() throws Exception {
        var dados = preparar(1);
        jdbcTemplate.update("update pecas_insumos set quantidade_reservada = 0 where id = ?", dados.insumo());
        mockMvc.perform(post("/ordens-servico/" + dados.ordens().getFirst() + "/finalizar"))
                .andExpect(status().isConflict());
        assertThat(statusDaOS(dados.ordens().getFirst())).isEqualTo("EM_EXECUCAO");
        assertThat(saldo(dados.peca(), "quantidade_total")).isEqualByComparingTo("10");
        assertThat(saldo(dados.peca(), "quantidade_reservada")).isEqualByComparingTo("1");
    }

    @Test
    void falhaDepoisDoConsumoReverteEstoqueEStatusNaMesmaTransacao() {
        var dados = preparar(1);
        doThrow(new IllegalStateException("Falha simulada ao salvar OS")).when(estadosOS).executa(any());
        assertThatThrownBy(() -> service.finalizar(dados.ordens().getFirst())).isInstanceOf(IllegalStateException.class);
        assertThat(statusDaOS(dados.ordens().getFirst())).isEqualTo("EM_EXECUCAO");
        assertThat(saldo(dados.peca(), "quantidade_total")).isEqualByComparingTo("10");
        assertThat(saldo(dados.insumo(), "quantidade_total")).isEqualByComparingTo("10");
        assertThat(saldo(dados.peca(), "quantidade_reservada")).isEqualByComparingTo("1");
        assertThat(saldo(dados.insumo(), "quantidade_reservada")).isEqualByComparingTo("1.125");
    }

    private void finalizarEmParalelo(UUID primeira, UUID segunda) throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var iniciar = new CountDownLatch(1);
            var a = executor.submit(() -> { iniciar.await(); return service.finalizar(primeira); });
            var b = executor.submit(() -> { iniciar.await(); return service.finalizar(segunda); });
            iniciar.countDown();
            a.get(30, TimeUnit.SECONDS);
            b.get(30, TimeUnit.SECONDS);
        }
    }

    @Test
    void chamadasConcorrentesDaMesmaOSBaixamUmaVez() throws Exception {
        var dados = preparar(1);
        finalizarEmParalelo(dados.ordens().getFirst(), dados.ordens().getFirst());
        assertThat(saldo(dados.peca(), "quantidade_total")).isEqualByComparingTo("9");
        assertThat(saldo(dados.insumo(), "quantidade_total")).isEqualByComparingTo("8.875");
        assertThat(statusDaOS(dados.ordens().getFirst())).isEqualTo("FINALIZADA");
    }

    @Test
    void ordensConcorrentesComMesmoEstoqueNaoPerdemBaixas() throws Exception {
        var dados = preparar(2);
        finalizarEmParalelo(dados.ordens().get(0), dados.ordens().get(1));
        assertThat(saldo(dados.peca(), "quantidade_total")).isEqualByComparingTo("8");
        assertThat(saldo(dados.insumo(), "quantidade_total")).isEqualByComparingTo("7.750");
        assertThat(saldo(dados.insumo(), "quantidade_reservada")).isZero();
        assertThat(statusDaOS(dados.ordens().get(0))).isEqualTo("FINALIZADA");
        assertThat(statusDaOS(dados.ordens().get(1))).isEqualTo("FINALIZADA");
    }
}

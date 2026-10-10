package com.techchallenge.oficina.pecasinsumos;

import com.jayway.jsonpath.JsonPath;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.support.PostgresTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class PecaInsumoApiIntegrationTest {

    private static final String MENSAGEM_DIGITOS_PRECO = "deve ter no máximo 10 dígitos inteiros e 2 casas decimais";
    private static final String MENSAGEM_DIGITOS_QUANTIDADE = "deve ter no máximo 9 dígitos inteiros e 3 casas decimais";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PecaInsumoRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    private static String json(String tipo, String codigo, String nome, String unidade, String preco,
                               String quantidadeInicial) {
        return """
                {"tipo":"%s","codigo":"%s","nome":"%s","unidadeMedida":"%s","precoUnitario":%s,"quantidadeInicial":%s}"""
                .formatted(tipo, codigo, nome, unidade, preco, quantidadeInicial);
    }

    private static String quantidade(String valor) {
        return "{\"quantidade\":%s}".formatted(valor);
    }

    private UUID cadastrar(String tipo, String codigo, String nome, String unidade, String quantidadeInicial)
            throws Exception {
        String corpo = mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(tipo, codigo, nome, unidade, "10.00", quantidadeInicial)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.id"));
    }

    private UUID cadastrarPeca(String codigo, String quantidadeInicial) throws Exception {
        return cadastrar("PECA", codigo, "Peça " + codigo, "UN", quantidadeInicial);
    }

    /** A reserva não tem endpoint (quem reserva é a Ordem de Serviço); aqui ela é feita direto pela entidade. */
    private void reservarNoBanco(UUID id, String quantidade) {
        PecaInsumo item = repository.findById(id).orElseThrow();
        item.reservar(new BigDecimal(quantidade));
        repository.saveAndFlush(item);
    }

    private void assertSaldoNoBanco(UUID id, String total, String reservada) {
        PecaInsumo item = repository.findById(id).orElseThrow();
        assertThat(item.getQuantidadeTotal()).isEqualByComparingTo(total);
        assertThat(item.getQuantidadeReservada()).isEqualByComparingTo(reservada);
    }

    @Test
    void fluxoCompletoDoCrudRefletidoNoBanco() throws Exception {
        // Create
        MvcResult criado = mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("INSUMO", " oleo-5w30 ", " Óleo 5W30 ", "L", "45.90", "20")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/pecas-insumos/")))
                .andExpect(jsonPath("$.codigo").value("OLEO-5W30"))
                .andExpect(jsonPath("$.nome").value("Óleo 5W30"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.createdAt").value(endsWith("-03:00")))
                // a resposta do cadastro já sai com as casas decimais do banco, igual à consulta
                .andExpect(content().string(containsString("\"precoUnitario\":45.90")))
                .andExpect(content().string(containsString("\"quantidadeTotal\":20.000")))
                .andReturn();
        String location = criado.getResponse().getHeader("Location");
        UUID id = UUID.fromString(JsonPath.read(criado.getResponse().getContentAsString(), "$.id"));
        assertSaldoNoBanco(id, "20", "0");

        // Read (id e lista)
        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadeMedida").value("L"))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(20.0))
                .andExpect(content().string(containsString("\"precoUnitario\":45.90")))
                .andExpect(content().string(containsString("\"quantidadeTotal\":20.000")));
        mockMvc.perform(get("/pecas-insumos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        // Update: muda dados cadastrais, mas não tipo, unidade nem saldo
        mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content("""
                        {"codigo":"oleo-5w40","nome":"Óleo 5W40","descricao":"Frasco de 1 L","precoUnitario":49.90}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("OLEO-5W40"))
                .andExpect(jsonPath("$.descricao").value("Frasco de 1 L"));
        PecaInsumo atualizado = repository.findById(id).orElseThrow();
        assertThat(atualizado.getPrecoUnitario()).isEqualByComparingTo("49.90");
        assertThat(atualizado.getTipo()).isEqualTo(TipoItem.INSUMO);
        assertSaldoNoBanco(id, "20", "0");

        // Delete lógico: sai da listagem, mas continua consultável por id
        mockMvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(repository.findById(id).orElseThrow().isAtivo()).isFalse();
        mockMvc.perform(get("/pecas-insumos")).andExpect(jsonPath("$.content", hasSize(0)));
        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void entradasESaidasAtualizamOSaldo() throws Exception {
        UUID id = cadastrarPeca("FILTRO", "10");

        mockMvc.perform(post("/pecas-insumos/{id}/entradas", id).contentType(MediaType.APPLICATION_JSON)
                        .content(quantidade("5")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeTotal").value(15.0));
        assertSaldoNoBanco(id, "15", "0");

        mockMvc.perform(post("/pecas-insumos/{id}/saidas", id).contentType(MediaType.APPLICATION_JSON)
                        .content(quantidade("12")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeDisponivel").value(3.0));
        assertSaldoNoBanco(id, "3", "0");
    }

    @Test
    void saidaMaiorQueODisponivelRetorna409ENaoAlteraSaldo() throws Exception {
        UUID id = cadastrarPeca("FILTRO", "10");

        mockMvc.perform(post("/pecas-insumos/{id}/saidas", id).contentType(MediaType.APPLICATION_JSON)
                        .content(quantidade("11")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(containsString("Estoque insuficiente")))
                .andExpect(jsonPath("$.detail").value(endsWith("solicitado 11, disponível 10")));
        assertSaldoNoBanco(id, "10", "0");
    }

    @Test
    void saidaNaoUsaAQuantidadeReservada() throws Exception {
        UUID id = cadastrarPeca("FILTRO", "10");
        reservarNoBanco(id, "8");

        mockMvc.perform(post("/pecas-insumos/{id}/saidas", id).contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("3"))).andExpect(status().isConflict());
        mockMvc.perform(post("/pecas-insumos/{id}/saidas", id).contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("2"))).andExpect(status().isOk());
        assertSaldoNoBanco(id, "8", "8");
    }

    @Test
    void quantidadeFracionadaEmUnidadeInteiraRetorna400() throws Exception {
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("PECA", "FILTRO", "Filtro", "UN", "10.00", "1.5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("não aceita ser fracionada")));
        assertThat(repository.count()).isZero();

        UUID id = cadastrarPeca("FILTRO", "10");
        mockMvc.perform(post("/pecas-insumos/{id}/entradas", id).contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("0.5"))).andExpect(status().isBadRequest());
        assertSaldoNoBanco(id, "10", "0");
    }

    @Test
    void valoresComMaisCasasDecimaisQueOBancoGuardaRetornam400() throws Exception {
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("PECA", "P1", "Peça", "UN", "10.999", "1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.campo=='precoUnitario')].mensagem").value(MENSAGEM_DIGITOS_PRECO));
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("PECA", "P1", "Peça", "UN", "10000000000000", "1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.campo=='precoUnitario')].mensagem").value(MENSAGEM_DIGITOS_PRECO));
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("INSUMO", "FLUIDO", "Fluido de freio", "L", "30", "1.2345")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.campo=='quantidadeInicial')].mensagem")
                        .value(MENSAGEM_DIGITOS_QUANTIDADE));
        assertThat(repository.count()).isZero();

        UUID id = cadastrar("INSUMO", "FLUIDO", "Fluido de freio", "L", "1");
        mockMvc.perform(post("/pecas-insumos/{id}/entradas", id).contentType(MediaType.APPLICATION_JSON)
                        .content(quantidade("0.0001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.campo=='quantidade')].mensagem").value(MENSAGEM_DIGITOS_QUANTIDADE));
        assertSaldoNoBanco(id, "1", "0");
    }

    @Test
    void codigoDuplicadoRetorna409NoCadastroENaAtualizacao() throws Exception {
        cadastrarPeca("FILTRO-AR", "1");
        UUID outro = cadastrarPeca("FILTRO-OLEO", "1");

        // o código é normalizado antes da checagem: "filtro-ar" é o mesmo que "FILTRO-AR"
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("PECA", "filtro-ar", "Outro", "UN", "1", "0")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("FILTRO-AR")));
        mockMvc.perform(put("/pecas-insumos/{id}", outro).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\" filtro-ar \",\"nome\":\"Filtro\",\"precoUnitario\":1}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/pecas-insumos/{id}", outro).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"filtro-oleo\",\"nome\":\"Filtro de óleo\",\"precoUnitario\":1}"))
                .andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void dadosInvalidosRetornam400ComErrosPorCampo() throws Exception {
        // mensagens do Bean Validation em português (spring.web.locale: pt_BR), mesmo pedindo inglês
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors", hasSize(5)))
                .andExpect(jsonPath("$.errors[?(@.campo=='tipo')].mensagem").value("não deve ser nulo"))
                .andExpect(jsonPath("$.errors[?(@.campo=='codigo')].mensagem").value("não deve estar em branco"));
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("PNEU", "P1", "Peça", "UN", "1", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("tipo"))
                .andExpect(jsonPath("$.errors[0].mensagem").value(containsString("PECA, INSUMO")));
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("PECA", "P1", "Peça", "UN", "-1", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.campo=='precoUnitario')]").exists());
        mockMvc.perform(post("/pecas-insumos").contentType(MediaType.APPLICATION_JSON).content("{nao-json"))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();

        UUID id = cadastrarPeca("FILTRO", "10");
        mockMvc.perform(post("/pecas-insumos/{id}/saidas", id).contentType(MediaType.APPLICATION_JSON)
                        .content(quantidade("0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.campo=='quantidade')]").exists());
    }

    @Test
    void listagemFiltraPorTipoENomeIgnoraInativosEValidaPaginacao() throws Exception {
        cadastrar("PECA", "FILTRO", "Filtro de oleo", "UN", "1");
        cadastrar("INSUMO", "OLEO", "Oleo 5W30", "L", "1");
        UUID removido = cadastrar("PECA", "PASTILHA", "Pastilha de freio", "UN", "1");
        mockMvc.perform(delete("/pecas-insumos/{id}", removido)).andExpect(status().isNoContent());

        mockMvc.perform(get("/pecas-insumos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nome").value("Filtro de oleo"));
        mockMvc.perform(get("/pecas-insumos").param("tipo", "PECA"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigo").value("FILTRO"));
        mockMvc.perform(get("/pecas-insumos").param("nome", "OLEO"))
                .andExpect(jsonPath("$.content", hasSize(2)));
        mockMvc.perform(get("/pecas-insumos").param("tipo", "INSUMO").param("nome", "5w30"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigo").value("OLEO"));
        mockMvc.perform(get("/pecas-insumos").param("size", "1").param("page", "1"))
                .andExpect(jsonPath("$.content[0].nome").value("Oleo 5W30"));

        mockMvc.perform(get("/pecas-insumos").param("size", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/pecas-insumos").param("size", "101")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/pecas-insumos").param("page", "-1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/pecas-insumos").param("tipo", "XYZ")).andExpect(status().isBadRequest());
    }

    @Test
    void itemRemovidoNaoAceitaMovimentacaoNemNovaRemocao() throws Exception {
        UUID id = cadastrarPeca("FILTRO", "10");
        mockMvc.perform(delete("/pecas-insumos/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(post("/pecas-insumos/{id}/entradas", id).contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("1"))).andExpect(status().isConflict());
        mockMvc.perform(post("/pecas-insumos/{id}/saidas", id).contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("1"))).andExpect(status().isConflict());
        mockMvc.perform(delete("/pecas-insumos/{id}", id)).andExpect(status().isConflict());
        assertSaldoNoBanco(id, "10", "0");
    }

    @Test
    void naoRemoveItemComReserva() throws Exception {
        UUID id = cadastrarPeca("FILTRO", "5");
        reservarNoBanco(id, "2");

        mockMvc.perform(delete("/pecas-insumos/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("reservada")));
        assertThat(repository.findById(id).orElseThrow().isAtivo()).isTrue();
    }

    @Test
    void codigoDeItemRemovidoFicaLivreParaNovoCadastro() throws Exception {
        UUID antigo = cadastrarPeca("FILTRO", "1");
        mockMvc.perform(delete("/pecas-insumos/{id}", antigo)).andExpect(status().isNoContent());

        UUID novo = cadastrarPeca("filtro", "3");

        assertThat(novo).isNotEqualTo(antigo);
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findById(antigo).orElseThrow().isAtivo()).isFalse();
        assertThat(repository.findById(novo).orElseThrow().isAtivo()).isTrue();
    }

    @Test
    void reativaItemRemovidoComOSaldoQueTinha() throws Exception {
        UUID id = cadastrarPeca("FILTRO", "4");
        mockMvc.perform(delete("/pecas-insumos/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(post("/pecas-insumos/{id}/reativacao", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.quantidadeTotal").value(4.0));
        mockMvc.perform(get("/pecas-insumos")).andExpect(jsonPath("$.content", hasSize(1)));
        mockMvc.perform(post("/pecas-insumos/{id}/entradas", id).contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("1"))).andExpect(status().isOk());
        assertSaldoNoBanco(id, "5", "0");
    }

    @Test
    void naoReativaEnquantoOCodigoEstiverEmUsoPorOutroItemAtivo() throws Exception {
        UUID antigo = cadastrarPeca("FILTRO", "1");
        mockMvc.perform(delete("/pecas-insumos/{id}", antigo)).andExpect(status().isNoContent());
        UUID novo = cadastrarPeca("FILTRO", "1");

        mockMvc.perform(post("/pecas-insumos/{id}/reativacao", antigo))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Já existe uma peça/insumo ativa com o código FILTRO"));
        assertThat(repository.findById(antigo).orElseThrow().isAtivo()).isFalse();

        // removido o item que ocupava o código, o antigo pode voltar
        mockMvc.perform(delete("/pecas-insumos/{id}", novo)).andExpect(status().isNoContent());
        mockMvc.perform(post("/pecas-insumos/{id}/reativacao", antigo)).andExpect(status().isOk());
    }

    @Test
    void reativarItemAtivoRetorna409() throws Exception {
        UUID id = cadastrarPeca("FILTRO", "1");

        mockMvc.perform(post("/pecas-insumos/{id}/reativacao", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("já está ativo")));
    }

    @Test
    void editaItemRemovidoMesmoComOCodigoUsadoPorOutroItemAtivo() throws Exception {
        UUID antigo = cadastrarPeca("FILTRO", "1");
        mockMvc.perform(delete("/pecas-insumos/{id}", antigo)).andExpect(status().isNoContent());
        cadastrarPeca("FILTRO", "1");

        mockMvc.perform(put("/pecas-insumos/{id}", antigo).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"FILTRO\",\"nome\":\"Filtro antigo\",\"precoUnitario\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Filtro antigo"));
    }

    @Test
    void bancoImpedeDoisItensAtivosComOMesmoCodigo() {
        PecaInsumo removido = new PecaInsumo(TipoItem.PECA, "FILTRO", "Filtro", null, UnidadeMedida.UN,
                BigDecimal.ONE, null);
        removido.desativar();
        repository.saveAndFlush(removido);
        repository.saveAndFlush(new PecaInsumo(TipoItem.PECA, "FILTRO", "Filtro", null, UnidadeMedida.UN,
                BigDecimal.ONE, null));

        assertThatThrownBy(() -> repository.saveAndFlush(new PecaInsumo(TipoItem.PECA, "FILTRO", "Outro", null,
                UnidadeMedida.UN, BigDecimal.ONE, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void idInexistenteRetorna404EIdMalformadoRetorna400() throws Exception {
        String inexistente = "/pecas-insumos/00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get(inexistente)).andExpect(status().isNotFound());
        mockMvc.perform(put(inexistente).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"X\",\"nome\":\"X\",\"precoUnitario\":1}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(inexistente)).andExpect(status().isNotFound());
        mockMvc.perform(post(inexistente + "/entradas").contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("1"))).andExpect(status().isNotFound());
        mockMvc.perform(post(inexistente + "/saidas").contentType(MediaType.APPLICATION_JSON)
                .content(quantidade("1"))).andExpect(status().isNotFound());
        mockMvc.perform(post(inexistente + "/reativacao")).andExpect(status().isNotFound());
        mockMvc.perform(get("/pecas-insumos/abc")).andExpect(status().isBadRequest());
    }
}

package com.techchallenge.oficina.veiculos;

import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import com.techchallenge.oficina.support.AdminAutenticadoTestConfiguration;
import com.techchallenge.oficina.support.PostgresTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
@Import({PostgresTestConfiguration.class, AdminAutenticadoTestConfiguration.class})
class VeiculoApiIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	VeiculoRepository repository;

	@BeforeEach
	void limparBanco() {
		repository.deleteAll();
	}

	private static String json(String placa, String marca, String modelo, Integer ano) {
		return """
				{"placa":"%s","marca":"%s","modelo":"%s","ano":%s}""".formatted(placa, marca, modelo, ano);
	}

	private String criar(String placa) throws Exception {
		return mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(json(placa, "Fiat", "Uno", 2010))).andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");
	}

	@Test
	void fluxoCompletoDoCrudRefletidoNoBanco() throws Exception {
		var criado = mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(json("abc-1234", " Fiat ", " Uno ", 2010)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/veiculos/")))
				.andExpect(jsonPath("$.placa").value("ABC1234"))
				.andExpect(jsonPath("$.marca").value("Fiat"))
				.andExpect(jsonPath("$.modelo").value("Uno"))
				.andExpect(jsonPath("$.ano").value(2010))
				.andExpect(jsonPath("$.createdAt").value(endsWith("-03:00")))
				.andExpect(jsonPath("$.updatedAt").value(endsWith("-03:00"))).andReturn();
		String location = criado.getResponse().getHeader("Location");
		assertThat(repository.findByPlaca("ABC1234")).isPresent();

		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.modelo").value("Uno"));
		mockMvc.perform(get("/veiculos")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.page.totalElements").value(1));
		mockMvc.perform(get("/veiculos").param("placa", "abc-1234")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].placa").value("ABC1234"));
		mockMvc.perform(get("/veiculos").param("placa", "DEF5678")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(0)));

		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
				.content(json("abc1d23", " Ford ", " Ka ", 2020))).andExpect(status().isOk())
				.andExpect(jsonPath("$.placa").value("ABC1D23")).andExpect(jsonPath("$.marca").value("Ford"))
				.andExpect(jsonPath("$.modelo").value("Ka")).andExpect(jsonPath("$.ano").value(2020));
		assertThat(repository.findByPlaca("ABC1D23")).isPresent();
		assertThat(repository.findByPlaca("ABC1234")).isEmpty();

		mockMvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
		assertThat(repository.count()).isZero();
		mockMvc.perform(get(location)).andExpect(status().isNotFound());
		mockMvc.perform(delete(location)).andExpect(status().isNotFound());
	}

	@ParameterizedTest
	@ValueSource(strings = {"ABC1234", "ABC-1234", "abc1234", "ABC1D23", "abc1d23"})
	void aceitaFormatosDePlaca(String placa) throws Exception {
		criar(placa);
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void dadosInvalidosRetornam400ComErrosPorCampo() throws Exception {
		mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(json("ABC123", " ", "", 0))).andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[?(@.campo=='placa')].mensagem").value("Placa inválida"))
				.andExpect(jsonPath("$.errors[?(@.campo=='marca')]").exists())
				.andExpect(jsonPath("$.errors[?(@.campo=='modelo')]").exists())
				.andExpect(jsonPath("$.errors[?(@.campo=='ano')]").exists());
		assertThat(repository.count()).isZero();
	}

	@ParameterizedTest
	@ValueSource(strings = {"{}", "{\"placa\":null,\"marca\":null,\"modelo\":null,\"ano\":null}",
			"{\"placa\":\"\",\"marca\":\"\",\"modelo\":\"\",\"ano\":null}"})
	void camposObrigatoriosAusentesRetornam400(String body) throws Exception {
		mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors", hasSize(4)));
		assertThat(repository.count()).isZero();
	}

	@Test
	void validaLimitesDeMarcaModeloEAno() throws Exception {
		mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(json("ABC1234", "A".repeat(76), "B".repeat(76), 10000)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors", hasSize(3)));
	}

	@Test
	void atualizacaoInvalidaNaoAlteraBanco() throws Exception {
		String location = criar("ABC1234");
		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
				.content(json("ABC-1D23", " ", "", null))).andExpect(status().isBadRequest());
		assertThat(repository.findByPlaca("ABC1234").orElseThrow().getModelo()).isEqualTo("Uno");
	}

	@Test
	void placaDuplicadaRetorna409NoCadastroENaAtualizacao() throws Exception {
		criar("ABC1234");
		mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(json("abc-1234", "Ford", "Ka", 2020))).andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
		String location = criar("DEF5678");
		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
				.content(json("abc-1234", "Ford", "Ka", 2020))).andExpect(status().isConflict());
		assertThat(repository.count()).isEqualTo(2);
		assertThat(repository.findByPlaca("DEF5678").orElseThrow().getModelo()).isEqualTo("Uno");
	}

	@Test
	void atualizarMantendoPropriaPlacaNaoEhConflito() throws Exception {
		String location = criar("ABC1234");
		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
				.content(json("abc-1234", "Ford", "Ka", 2020))).andExpect(status().isOk())
				.andExpect(jsonPath("$.placa").value("ABC1234")).andExpect(jsonPath("$.modelo").value("Ka"));
	}

	@Test
	void bancoImpedePlacaDuplicada() throws Exception {
		criar("ABC1234");
		assertThatThrownBy(() -> repository.saveAndFlush(new Veiculo("ABC1234", "Ford", "Ka", 2020)))
				.isInstanceOf(DataIntegrityViolationException.class);
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void listagemPaginadaValidaPageESizeEIgnoraSortLivre() throws Exception {
		criar("DEF5678");
		criar("ABC1234");
		mockMvc.perform(get("/veiculos").param("sort", "string")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].placa").value("ABC1234"));
		mockMvc.perform(get("/veiculos").param("page", "1").param("size", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.content[0].placa").value("DEF5678"));
		mockMvc.perform(get("/veiculos").param("size", "0")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/veiculos").param("size", "101")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/veiculos").param("page", "-1")).andExpect(status().isBadRequest());
	}

	@Test
	void idInexistenteRetorna404EIdMalformadoRetorna400() throws Exception {
		String inexistente = "/veiculos/00000000-0000-0000-0000-000000000000";
		mockMvc.perform(get(inexistente)).andExpect(status().isNotFound());
		mockMvc.perform(put(inexistente).contentType(MediaType.APPLICATION_JSON)
				.content(json("ABC1234", "Fiat", "Uno", 2010))).andExpect(status().isNotFound());
		mockMvc.perform(delete(inexistente)).andExpect(status().isNotFound());
		mockMvc.perform(get("/veiculos/abc")).andExpect(status().isBadRequest());
	}

	@Test
	void jsonMalformadoRetorna400() throws Exception {
		mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content("{nao-json"))
				.andExpect(status().isBadRequest());
	}
}

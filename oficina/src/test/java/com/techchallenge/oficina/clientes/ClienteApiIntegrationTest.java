package com.techchallenge.oficina.clientes;

import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.support.AdminAutenticadoTestConfiguration;
import com.techchallenge.oficina.support.PostgresTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
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
class ClienteApiIntegrationTest {

	private static final String CPF = "529.982.247-25";
	private static final String CNPJ = "11.222.333/0001-81";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ClienteRepository repository;

	@BeforeEach
	void limparBanco() {
		repository.deleteAll();
	}

	private static String json(String firstName, String lastName, String email, String documento, String telefone) {
		String tel = telefone == null ? "null" : "\"" + telefone + "\"";
		return """
				{"firstName":"%s","lastName":"%s","email":"%s","documento":"%s","telefone":%s}"""
				.formatted(firstName, lastName, email, documento, tel);
	}

	private String criar(String body) throws Exception {
		MvcResult result = mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated()).andReturn();
		return result.getResponse().getHeader("Location");
	}

	@Test
	void fluxoCompletoDoCrudRefletidoNoBanco() throws Exception {
		// Create
		MvcResult criado = mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
						.content(json("Maria", "Silva", "maria@email.com", CPF, "11999990000")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/clientes/")))
				.andExpect(jsonPath("$.fullName").value("Maria Silva"))
				.andExpect(jsonPath("$.documento").value("52998224725"))
				.andExpect(jsonPath("$.tipoDocumento").value("CPF"))
				.andExpect(jsonPath("$.createdAt").value(org.hamcrest.Matchers.endsWith("-03:00")))
				.andExpect(jsonPath("$.updatedAt").value(org.hamcrest.Matchers.endsWith("-03:00")))
				.andReturn();
		String location = criado.getResponse().getHeader("Location");
		assertThat(repository.count()).isEqualTo(1);
		assertThat(repository.findByDocumento("52998224725")).isPresent();

		// Read (id)
		mockMvc.perform(get(location)).andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("maria@email.com"))
				.andExpect(jsonPath("$.telefone").value("11999990000"));

		// Read (lista e filtro por documento)
		mockMvc.perform(get("/clientes")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.page.totalElements").value(1));
		mockMvc.perform(get("/clientes").param("documento", CPF)).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].fullName").value("Maria Silva"));
		mockMvc.perform(get("/clientes").param("documento", CNPJ)).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(0)));

		// Update (troca para CNPJ)
		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
						.content(json("Oficina", "Silva LTDA", "contato@silva.com", CNPJ, null)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Oficina Silva LTDA"))
				.andExpect(jsonPath("$.tipoDocumento").value("CNPJ"))
				.andExpect(jsonPath("$.telefone").doesNotExist());
		assertThat(repository.findByDocumento("11222333000181")).isPresent();
		assertThat(repository.findByDocumento("52998224725")).isEmpty();

		// Delete
		mockMvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
		assertThat(repository.count()).isZero();
		mockMvc.perform(get(location)).andExpect(status().isNotFound());
		mockMvc.perform(delete(location)).andExpect(status().isNotFound());
	}

	@Test
	void cadastroComDadosInvalidosRetorna400ComErrosPorCampo() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
						.content(json("", "Silva", "nao-e-email", "123.456.789-00", null)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[?(@.campo=='firstName')]").exists())
				.andExpect(jsonPath("$.errors[?(@.campo=='email')]").exists())
				.andExpect(jsonPath("$.errors[?(@.campo=='documento')].mensagem").value("CPF ou CNPJ inválido"));
		assertThat(repository.count()).isZero();
	}

	@Test
	void listagemPaginadaIgnoraSortLivreEValidaPageESize() throws Exception {
		criar(json("Ana", "Zeta", "ana@email.com", CPF, null));
		criar(json("Bia", "Alfa", "bia@email.com", "111.444.777-35", null));

		// o Swagger UI envia sort=string / ["desc"]: não pode virar 500
		mockMvc.perform(get("/clientes").param("page", "0").param("size", "20").param("sort", "string"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[0].fullName").value("Ana Zeta"));
		mockMvc.perform(get("/clientes").param("sort", "[\"desc\"]")).andExpect(status().isOk());
		mockMvc.perform(get("/clientes").param("size", "1").param("page", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].fullName").value("Bia Alfa"));
		mockMvc.perform(get("/clientes").param("size", "0")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/clientes").param("size", "101")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/clientes").param("page", "-1")).andExpect(status().isBadRequest());
	}

	@Test
	void jsonMalformadoRetorna400() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("{nao-json"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void documentoDuplicadoRetorna409NoPostEmQualquerMascara() throws Exception {
		criar(json("Maria", "Silva", "maria@email.com", CPF, null));

		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
						.content(json("Outra", "Pessoa", "outra@email.com", "52998224725", null)))
				.andExpect(status().isConflict());
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void atualizarComDocumentoDeOutroClienteRetorna409() throws Exception {
		criar(json("Maria", "Silva", "maria@email.com", CPF, null));
		String location = criar(json("Joao", "Souza", "joao@email.com", "111.444.777-35", null));

		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
						.content(json("Joao", "Souza", "joao@email.com", CPF, null)))
				.andExpect(status().isConflict());
	}

	@Test
	void atualizarMantendoProprioDocumentoNaoEhConflito() throws Exception {
		String location = criar(json("Maria", "Silva", "maria@email.com", CPF, null));

		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
						.content(json("Maria", "Santos", "maria@email.com", CPF, null)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Maria Santos"));
	}

	@Test
	void idInexistenteRetorna404EIdMalformadoRetorna400() throws Exception {
		String inexistente = "/clientes/00000000-0000-0000-0000-000000000000";
		mockMvc.perform(get(inexistente)).andExpect(status().isNotFound());
		mockMvc.perform(put(inexistente).contentType(MediaType.APPLICATION_JSON)
						.content(json("A", "B", "a@b.com", CPF, null)))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete(inexistente)).andExpect(status().isNotFound());
		mockMvc.perform(get("/clientes/abc")).andExpect(status().isBadRequest());
	}
}

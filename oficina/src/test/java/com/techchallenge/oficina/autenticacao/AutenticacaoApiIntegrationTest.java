package com.techchallenge.oficina.autenticacao;

import com.jayway.jsonpath.JsonPath;
import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class AutenticacaoApiIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtEncoder jwtEncoder;

	@Autowired
	JwtDecoder jwtDecoder;

	@Value("${seguranca.admin.username}")
	String adminUsername;

	@Value("${seguranca.admin.password}")
	String adminPassword;

	@Value("${seguranca.cliente.username}")
	String clienteUsername;

	@Value("${seguranca.cliente.password}")
	String clientePassword;

	@Value("${seguranca.cliente.documento}")
	String clienteDocumento;

	@Value("${seguranca.jwt.expiracao-minutos}")
	long expiracaoMinutos;

	private static String json(String username, String password) {
		return """
				{"username":"%s","password":"%s"}""".formatted(username, password);
	}

	private String login(String username, String password) throws Exception {
		String resposta = mockMvc.perform(post("/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
						.content(json(username, password)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.accessToken");
	}

	private String tokenAssinado(JwtClaimsSet claims) {
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

	@Test
	void loginComCredenciaisValidasRetornaToken() throws Exception {
		mockMvc.perform(post("/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
						.content(json(adminUsername, adminPassword)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value(not(emptyString())))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(expiracaoMinutos * 60));
	}

	@Test
	void loginComSenhaErradaRetorna401() throws Exception {
		mockMvc.perform(post("/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
						.content(json(adminUsername, "senha-errada")))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Usuário ou senha inválidos"));
		mockMvc.perform(post("/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
						.content(json(clienteUsername, adminPassword)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginSemCamposRetorna400() throws Exception {
		mockMvc.perform(post("/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[?(@.campo=='username')]").exists())
				.andExpect(jsonPath("$.errors[?(@.campo=='password')]").exists());
	}

	@Test
	void tokenLevaPerfilEOClienteLevaOProprioDocumento() throws Exception {
		Jwt admin = jwtDecoder.decode(login(adminUsername, adminPassword));
		assertThat(admin.getSubject()).isEqualTo(adminUsername);
		assertThat(admin.getClaimAsStringList(Perfil.CLAIM_JWT)).containsExactly("ADMIN");
		assertThat(admin.hasClaim(ControleDeAcesso.CLAIM_DOCUMENTO)).isFalse();

		Jwt cliente = jwtDecoder.decode(login(clienteUsername, clientePassword));
		assertThat(cliente.getSubject()).isEqualTo(clienteUsername);
		assertThat(cliente.getClaimAsStringList(Perfil.CLAIM_JWT)).containsExactly("CLIENTE");
		assertThat(cliente.getClaimAsString(ControleDeAcesso.CLAIM_DOCUMENTO)).isEqualTo(clienteDocumento);
	}

	@Test
	void adminAcessaApisAdministrativas() throws Exception {
		String token = login(adminUsername, adminPassword);
		mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(token)))
				.andExpect(status().isOk());
		mockMvc.perform(get("/pecas-insumos").header(HttpHeaders.AUTHORIZATION, bearer(token)))
				.andExpect(status().isOk());
	}

	@Test
	void clienteAutenticadoRecebe403NasApisAdministrativas() throws Exception {
		String token = login(clienteUsername, clientePassword);
		mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(token)))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(token)))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/pecas-insumos").header(HttpHeaders.AUTHORIZATION, bearer(token))
						.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void tokenValidoSemPerfilRecebe403() throws Exception {
		Instant agora = Instant.now();
		String semPerfil = tokenAssinado(JwtClaimsSet.builder()
				.subject("alguem")
				.issuedAt(agora)
				.expiresAt(agora.plus(Duration.ofMinutes(5)))
				.build());

		mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(semPerfil)))
				.andExpect(status().isForbidden());
	}

	@Test
	void semTokenOuComTokenInvalidoRetorna401() throws Exception {
		mockMvc.perform(get("/clientes")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/pecas-insumos")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer("token-invalido")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenExpiradoRetorna401() throws Exception {
		Instant emitidoEm = Instant.now().minus(Duration.ofHours(2));
		String expirado = tokenAssinado(JwtClaimsSet.builder()
				.subject(adminUsername)
				.issuedAt(emitidoEm)
				.expiresAt(emitidoEm.plus(Duration.ofHours(1)))
				.claim(Perfil.CLAIM_JWT, List.of("ADMIN"))
				.build());

		mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(expirado)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void documentacaoDaApiEhPublicaEDeclaraOBearer() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
	}
}
